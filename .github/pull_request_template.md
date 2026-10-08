## What and why

## How it was tested

- [ ] `./gradlew build` passes (unit tests and server GameTests)
- [ ] New behaviour has a test that fails without the change
- [ ] Touched reef rules or the ticker: `python3 -P tools/mutation.py` still kills every mutant
- [ ] Touched the random tick handler: `tools/benchmark.sh` before and after, numbers in the PR
- [ ] Chat output or visuals changed: media regenerated (`runClientGameTest` + `branding/make_media.sh`)
