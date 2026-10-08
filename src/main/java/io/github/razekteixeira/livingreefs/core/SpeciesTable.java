package io.github.razekteixeira.livingreefs.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Pairs every coral block id with its species, form and living or dead twin.
 *
 * <p>Vanilla only links living to dead (a field on the living block), and tags list living coral
 * only, so the pairing comes from the id pattern vanilla uses: {@code <ns>:<type>_coral_block},
 * {@code _coral}, {@code _coral_fan} and {@code _coral_wall_fan}, with a {@code dead_} prefix for the
 * dead twin. Mods that name their coral the same way are paired too. A form is only known when both
 * its living and its dead id exist, so a half-defined coral is never turned into a missing block.
 */
public final class SpeciesTable {
	/** One coral block: its species ({@code namespace:type}), form and whether it is the dead twin. */
	public record Entry(String species, CoralForm form, boolean dead) {
	}

	private final Map<String, Entry> byId = new HashMap<>();
	private final Map<Entry, String> byEntry = new HashMap<>();

	private SpeciesTable() {
	}

	/** Builds the table from every block id in the game. Ids that are not coral are ignored. */
	public static SpeciesTable of(Collection<String> blockIds) {
		Set<String> ids = new HashSet<>(blockIds);
		SpeciesTable table = new SpeciesTable();
		for (String id : ids) {
			Optional<Entry> parsed = parse(id);
			if (parsed.isEmpty() || parsed.get().dead()) {
				continue;
			}
			Entry living = parsed.get();
			String deadId = deadIdOf(id);
			if (ids.contains(deadId)) {
				Entry dead = new Entry(living.species(), living.form(), true);
				table.put(id, living);
				table.put(deadId, dead);
			}
		}
		return table;
	}

	private void put(String id, Entry entry) {
		byId.put(id, entry);
		byEntry.put(entry, id);
	}

	/** Reads species and form from an id, without checking that the twin exists. */
	static Optional<Entry> parse(String id) {
		int colon = id.indexOf(':');
		if (colon <= 0 || colon == id.length() - 1) {
			return Optional.empty();
		}
		String namespace = id.substring(0, colon);
		String path = id.substring(colon + 1);
		boolean dead = path.startsWith("dead_");
		if (dead) {
			path = path.substring("dead_".length());
		}
		for (CoralForm form : CoralForm.values()) {
			if (path.endsWith(form.suffix())) {
				String type = path.substring(0, path.length() - form.suffix().length());
				if (type.isEmpty() || type.startsWith("dead_")) {
					return Optional.empty();
				}
				return Optional.of(new Entry(namespace + ":" + type, form, dead));
			}
		}
		return Optional.empty();
	}

	private static String deadIdOf(String livingId) {
		int colon = livingId.indexOf(':');
		return livingId.substring(0, colon + 1) + "dead_" + livingId.substring(colon + 1);
	}

	public Optional<Entry> lookup(String id) {
		return Optional.ofNullable(byId.get(id));
	}

	/** The id of a species in a form, living or dead, if that block exists. */
	public Optional<String> id(String species, CoralForm form, boolean dead) {
		return Optional.ofNullable(byEntry.get(new Entry(species, form, dead)));
	}

	/** The living or dead twin of a coral id. */
	public Optional<String> twin(String id) {
		Entry entry = byId.get(id);
		return entry == null ? Optional.empty() : id(entry.species(), entry.form(), !entry.dead());
	}

	public int size() {
		return byId.size();
	}
}
