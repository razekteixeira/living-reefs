"""Mutation check for G4 Tests with teeth.

Plants each mutant below in turn (one source edit), runs the test task that should catch it, and
restores the file. A mutant only counts as killed when the build compiled and a named test failed;
a compile error or a run without a named failure counts as SURVIVED (or BROKEN), never as killed.

Usage, from the project root (JDK 25 as JAVA_HOME):
  python3 -P tools/mutation.py            # every mutant
  python3 -P tools/mutation.py M3 G2      # only these
Writes the result table to build/mutation-report.md and prints it.
"""

import re
import subprocess
import sys
import xml.etree.ElementTree as ElementTree
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MAIN = "src/main/java/io/github/razekteixeira/livingreefs/"
CORE = MAIN + "core/"

# (id, file, original text, mutated text, gradle task, test expected to kill it)
MUTANTS = [
    ("M1", CORE + "SpeciesTable.java", "if (ids.contains(deadId)) {", "if (true) {", "test",
     "SpeciesTableTest.formsWithoutBothTwinsAreLeftOut"),
    ("M2", CORE + "SpeciesTable.java", 'if (type.isEmpty() || type.startsWith("dead_")) {', "if (type.isEmpty()) {", "test",
     "SpeciesTableTest.parseRejectsMalformedIds"),
    ("M3", CORE + "ReefRules.java", "if (c.heatNearby()) {", "if (false) {", "test", "ReefRulesTest.heatStresses"),
    ("M4", CORE + "ReefRules.java", "if (!c.conduitNearby()) {", "if (true) {", "test",
     "ReefRulesTest.conduitMakesWaterReefAndProtects"),
    ("M5", CORE + "ReefRules.java", "boolean lit = c.light() >= s.minLight();", "boolean lit = c.light() > s.minLight();", "test",
     "ReefRulesTest.darkCoralIsDormant"),
    ("M6", CORE + "ReefRules.java", "yield roll < a.growthChance() + s.recoveryChance() ? Action.RECOVER : Action.NONE;",
     "yield roll < s.recoveryChance() ? Action.RECOVER : Action.NONE;", "test", "ReefRulesTest.decideUsesTheRollBands"),
    ("M7", CORE + "ReefRules.java", "return Math.min(1, Math.max(thriving, s.bleachChance()));", "return Math.min(1, thriving);", "test",
     "ReefRulesTest.gateCoversEveryAction"),
    ("M8", CORE + "SpreadPlanner.java", "for (int k = 1; k <= blocks; k++) {", "for (int k = 1; k < blocks; k++) {", "test",
     "SpreadPlannerTest.respectsWaterClearance"),
    ("M9", CORE + "SpreadPlanner.java", "&& nearbyCoral(view, dx, dy, dz) < s.maxNearbyCoral();",
     "&& nearbyCoral(view, dx, dy, dz) <= s.maxNearbyCoral();", "test", "SpreadPlannerTest.respectsDensityCap"),
    ("M10", CORE + "SpreadPlanner.java", "reefHeightBelow(view, table, dx, dy, dz) < s.maxReefHeight()) {",
     "reefHeightBelow(view, table, dx, dy, dz) <= s.maxReefHeight()) {", "test", "SpreadPlannerTest.coralBlockHeightIsCapped"),
    ("M11", CORE + "SpreadPlanner.java",
     "// The fan points away from the block it hangs on.\n\t\t\tif (view.cell(dx - facing.dx(), dy, dz - facing.dz()).substrate()) {",
     "// The fan points away from the block it hangs on.\n\t\t\tif (view.cell(dx + facing.dx(), dy, dz + facing.dz()).substrate()) {", "test", "SpreadPlannerTest.wallFanFacesAwayFromSupport"),
    ("M12", CORE + "SpreadPlanner.java", ".filter(e -> e.dead() && e.species().equals(source.get().species()))",
     ".filter(e -> e.dead())", "test", "SpreadPlannerTest.recoveryNeverTouchesDryOrOtherSpeciesOrLiving"),
    ("M13", CORE + "SpreadPlanner.java", "if (!target.isCoral() || !target.wet()) {", "if (!target.isCoral()) {", "test",
     "SpreadPlannerTest.recoveryNeverTouchesDryOrOtherSpeciesOrLiving"),
    ("M14", CORE + "Settings.java", "double clamped = Double.isNaN(value) ? min : Math.max(min, Math.min(max, value));",
     "double clamped = Math.max(min, Math.min(max, value));", "test", "SettingsTest.notANumberIsClampedNotKept"),
    ("M15", MAIN + "LivingReefsConfig.java", "current = checked.settings();", "current = parsed;", "test",
     "LivingReefsConfigTest.outOfRangeValuesAreClampedOnLoad"),
    ("M16", CORE + "SpreadPlanner.java", "if ((dx != 0 || dy != 0 || dz != 0) && room(view, dx, dy, dz, s)",
     "if ((dx != 0 || dy != 0 || dz != 0) && view.cell(dx, dy, dz).water()", "test",
     "SpreadPlannerTest.openSpotsCountWhatGrowthCouldUse"),
    ("G1", MAIN + "LivingCoral.java", "|| block instanceof CoralFanBlock || block instanceof CoralWallFanBlock;",
     "|| block instanceof CoralFanBlock;", "runGameTest", "livingCoralRandomTicksDeadDoesNot"),
    ("G2", MAIN + "mixin/BlockBehaviourMixin.java", "ReefTicker.onRandomTick(state, level, pos, random);", "", "runGameTest",
     "randomTicksGrowAReef"),
    ("G3", MAIN + "ReefTicker.java", "level.setBlock(pos, dead.get().withPropertiesOf(state), Block.UPDATE_ALL);",
     "level.setBlock(pos, dead.get().defaultBlockState(), Block.UPDATE_ALL);", "runGameTest", "heatBleachesCoral"),
    ("G4", MAIN + "WorldReefView.java", "boolean substrate = state.is(ReefTags.REEF_SUBSTRATE) && state.isCollisionShapeFullBlock(level, pos);",
     "boolean substrate = state.isCollisionShapeFullBlock(level, pos);", "runGameTest", "glassFloorStopsGrowth"),
    ("G5", MAIN + "ReefConditions.java", "if (near.is(ReefTags.REEF_HEAT_SOURCES)) {", "if (false) {", "runGameTest",
     "heatBleachesCoral"),
    ("G6", MAIN + "ReefConditions.java", "&& conduit.getBlockPos().distSqr(pos) <= radiusSquared) {\n\t\t\t\t\t\treturn true;",
     "&& conduit.getBlockPos().distSqr(pos) <= radiusSquared) {\n\t\t\t\t\t\treturn false;", "runGameTest", "conduitProtectsFromHeat"),
    ("G7", MAIN + "ReefTicker.java", "boolean growth = rules.get(ReefGameRules.CORAL_GROWTH);", "boolean growth = true;", "runGameTest",
     "randomTicksGrowAReef"),
    ("G8", MAIN + "LivingReefsCommands.java", "if (entry.dead()) {", "if (false) {", "runGameTest", "checkCommandExplainsHealth"),
    ("G9", MAIN + "ReefTicker.java", "if (p.facing() != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {",
     "if (false) {", "runGameTest", "wallFansGrowOnEverySide"),
    ("G10", MAIN + "ReefTicker.java", "case WEST -> Direction.WEST;", "case WEST -> Direction.EAST;", "runGameTest",
     "wallFansGrowOnEverySide"),
    ("G11", MAIN + "ReefTicker.java", "boolean bleaching = rules.get(ReefGameRules.CORAL_BLEACHING);", "boolean bleaching = true;",
     "runGameTest", "bleachingRuleSwitchesBleachingOff"),
    ("G12", MAIN + "ReefConditions.java", "light = Math.max(light, brightness(level, pos.relative(direction)));",
     "light = Math.max(light, brightness(level, pos));", "runGameTest", "darkCoralWaitsForLight"),
    ("G13", MAIN + "ReefTicker.java", "if (revive && ReefConditions.heatNear(level, target)) {", "if (false) {", "runGameTest",
     "noRecoveryNextToHeat"),
    ("G14", MAIN + "LivingReefsCommands.java",
     "if (!canAdmin(source) && !Vec3.atCenterOf(pos).closerThan(source.getPosition(), CHECK_RANGE)) {", "if (false) {",
     "runGameTest", "checkFarAwayNeedsAdmin"),
]


def gradle(task):
    result = subprocess.run(["./gradlew", task, "--console=plain", "--continue"], cwd=ROOT, capture_output=True, text=True)
    return result.returncode, result.stdout + result.stderr


def junit_failures():
    names = []
    for report in (ROOT / "build/test-results/test").glob("*.xml"):
        for case in ElementTree.parse(report).getroot().iter("testcase"):
            if case.find("failure") is not None or case.find("error") is not None:
                names.append(case.get("classname").rsplit(".", 1)[-1] + "." + case.get("name").split("(")[0])
    return names


# Fabric names a GameTest <mod id>:<class>_<method> in snake case, for example
# "livingreefs-gametest:living_reefs_game_tests_heat_bleaches_coral failed at ...".
GAMETEST_FAILURE = re.compile(r"livingreefs-gametest:living_reefs_game_tests_([a-z0-9_]+) failed")


def snake(name):
    return re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", name).lower()


def gametest_failures(output):
    return sorted({match.group(1) for match in GAMETEST_FAILURE.finditer(output)})


def run(mutant):
    mid, file, original, mutated, task, expected = mutant
    path = ROOT / file
    source = path.read_text()
    if source.count(original) != 1:
        return mid, expected, "INVALID (original text not found exactly once)", ""
    try:
        path.write_text(source.replace(original, mutated))
        code, output = gradle(task)
    finally:
        path.write_text(source)
    logs = ROOT / "build/mutation-logs"
    logs.mkdir(parents=True, exist_ok=True)
    (logs / f"{mid}.log").write_text(output)
    if "Compilation failed" in output or re.search(r"\.java:\d+: error:", output):
        return mid, expected, "BROKEN (compile error, does not count)", ""
    failed = junit_failures() if task == "test" else gametest_failures(output)
    if code != 0 and failed:
        wanted = expected if task == "test" else snake(expected)
        hit = wanted in failed
        return mid, expected, "KILLED" if hit else "KILLED (by other tests)", ", ".join(failed)
    return mid, expected, "SURVIVED", ""


def main():
    wanted = set(sys.argv[1:])
    rows = [run(m) for m in MUTANTS if not wanted or m[0] in wanted]
    lines = ["| Mutant | Expected killer | Result | Failing tests |", "|---|---|---|---|"]
    lines += [f"| {mid} | `{expected}` | {result} | {failed} |" for mid, expected, result, failed in rows]
    report = "\n".join(lines)
    (ROOT / "build").mkdir(exist_ok=True)
    (ROOT / "build/mutation-report.md").write_text(report + "\n")
    print(report)
    # Only a kill by the named test counts: an incidental kill by another test leaves the gate open.
    if any(result != "KILLED" for _, _, result, _ in rows):
        sys.exit(1)


if __name__ == "__main__":
    main()
