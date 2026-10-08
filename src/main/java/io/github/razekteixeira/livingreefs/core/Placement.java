package io.github.razekteixeira.livingreefs.core;

import org.jspecify.annotations.Nullable;

/**
 * A block to set, at an offset from the source coral.
 *
 * @param id     the coral block id to place
 * @param facing for wall fans, the direction the fan points (away from the block it hangs on)
 */
public record Placement(int dx, int dy, int dz, String id, @Nullable Facing facing) {
}
