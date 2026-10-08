"""Renders the Living Reefs icon as a 3D voxel diorama with Blender (Cycles).

Every pixel of branding/icon-32.png becomes a bevelled block; its height depends on what it is
(water tile, seabed, coral or bubble), so the logo reads as a small physical reef. Original art
only: the input is our own icon, no game textures.

Run from the project root:
  /Applications/Blender.app/Contents/MacOS/Blender -b -P branding/render_logo3d.py -- branding/logo3d.png
"""

import math
import sys

import bmesh
import bpy

ICON = "branding/icon-32.png"
OUT = sys.argv[sys.argv.index("--") + 1] if "--" in sys.argv else "branding/logo3d.png"

# Exact icon colours (from branding/make_icon.py) mapped to a layer.
LAYER_BY_COLOUR = {
    (4, 14, 24): "frame",
    (18, 92, 112): "water", (13, 70, 96): "water", (10, 52, 80): "water", (8, 38, 64): "water",
    (64, 160, 170): "caustic",
    (34, 8, 24): "outline",
    (255, 104, 124): "coral", (255, 176, 168): "coral", (196, 52, 92): "coral",
    (255, 214, 64): "fan", (204, 150, 28): "fan", (255, 240, 150): "fan",
    (150, 92, 230): "tube", (98, 56, 170): "tube", (206, 170, 255): "tube",
    (222, 200, 140): "sand", (184, 158, 102): "sand",
    (90, 98, 104): "rock",
    (190, 236, 244): "bubble",
}
# Block heights per layer; an outline pixel takes the height of the object it outlines.
HEIGHT = {"frame": 1.0, "water": 0.5, "caustic": 0.62, "sand": 1.3, "rock": 1.8,
          "tube": 2.6, "fan": 2.4, "coral": 3.4, "bubble": 3.0, "outline": 0.5}
# (metallic, roughness, emission strength) per layer.
SURFACE = {"frame": (0.2, 0.45, 0.0), "water": (0.0, 0.25, 0.0), "caustic": (0.0, 0.2, 1.2),
           "sand": (0.0, 0.8, 0.0), "rock": (0.0, 0.7, 0.0), "tube": (0.0, 0.4, 0.25),
           "fan": (0.0, 0.4, 0.25), "coral": (0.0, 0.38, 0.3), "bubble": (0.0, 0.05, 1.5),
           "outline": (0.0, 0.5, 0.0)}


def srgb_to_linear(c):
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4


def reset_scene():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    scene = bpy.context.scene
    scene.render.engine = "CYCLES"
    scene.cycles.samples = int(sys.argv[sys.argv.index("--") + 2]) if len(sys.argv) > sys.argv.index("--") + 2 else 256
    scene.cycles.use_denoising = True
    try:
        prefs = bpy.context.preferences.addons["cycles"].preferences
        prefs.compute_device_type = "METAL"
        prefs.get_devices()
        for device in prefs.devices:
            device.use = True
        scene.cycles.device = "GPU"
    except Exception as error:  # CPU fallback keeps the script portable
        print("GPU unavailable, using CPU:", error)
    scene.render.film_transparent = True
    scene.render.resolution_x = 1600
    scene.render.resolution_y = 1600
    scene.render.image_settings.file_format = "PNG"
    scene.render.image_settings.color_mode = "RGBA"
    scene.view_settings.view_transform = "AgX"
    scene.view_settings.look = "AgX - Punchy"
    scene.view_settings.exposure = 0.9
    world = bpy.data.worlds.new("World")
    world.use_nodes = True
    world.node_tree.nodes["Background"].inputs[0].default_value = (0.008, 0.02, 0.03, 1)
    world.node_tree.nodes["Background"].inputs[1].default_value = 1.0
    scene.world = world
    return scene


def material(name, metallic, roughness, emission):
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    bsdf = nodes["Principled BSDF"]
    attr = nodes.new("ShaderNodeAttribute")
    attr.attribute_name = "Col"
    mat.node_tree.links.new(attr.outputs["Color"], bsdf.inputs["Base Color"])
    bsdf.inputs["Metallic"].default_value = metallic
    bsdf.inputs["Roughness"].default_value = roughness
    if emission:
        mat.node_tree.links.new(attr.outputs["Color"], bsdf.inputs["Emission Color"])
        bsdf.inputs["Emission Strength"].default_value = emission
    return mat


def build_voxels():
    image = bpy.data.images.load(bpy.path.abspath("//" + ICON) if bpy.data.filepath else ICON)
    w, h = image.size
    px = list(image.pixels)
    pixels = {}
    for y in range(h):
        for x in range(w):
            i = ((h - 1 - y) * w + x) * 4  # Blender stores rows bottom-up
            r, g, b, a = px[i:i + 4]
            if a < 0.5:
                continue
            key = tuple(round(c * 255) for c in (r, g, b))
            layer = LAYER_BY_COLOUR.get(key)
            if layer is None:
                raise SystemExit(f"unknown icon colour {key} at {x},{y}: update LAYER_BY_COLOUR")
            pixels[(x, y)] = (layer, (r, g, b))

    def height(x, y):
        layer, _ = pixels[(x, y)]
        if layer != "outline":
            return HEIGHT[layer]
        # Outlines rise with the tallest object they touch, so shapes read as solid pieces.
        around = [pixels.get((x + dx, y + dy), ("water", None))[0] for dx in (-1, 0, 1) for dy in (-1, 0, 1)]
        return max(0.5, max(HEIGHT[n] for n in around if n != "outline") - 0.8)

    layers = {}
    for (x, y), (layer, rgb) in pixels.items():
        layers.setdefault(layer, []).append((x, y, rgb))
    for layer, items in layers.items():
        mesh = bpy.data.meshes.new(layer)
        bm = bmesh.new()
        colors = []
        for x, y, rgb in items:
            top = height(x, y)
            bottom = top - 0.9 if layer == "bubble" else 0.0
            cx, cy = x + 0.5, -(y + 0.5)
            half = 0.4 if layer == "bubble" else 0.5
            verts = [bm.verts.new((cx + sx * half, cy + sy * half, z))
                     for z in (bottom, top) for sx, sy in ((-1, -1), (1, -1), (1, 1), (-1, 1))]
            faces = [(0, 3, 2, 1), (4, 5, 6, 7), (0, 1, 5, 4), (1, 2, 6, 5), (2, 3, 7, 6), (3, 0, 4, 7)]
            for f in faces:
                bm.faces.new([verts[k] for k in f])
                colors.append(tuple(srgb_to_linear(c) for c in rgb) + (1.0,))
        bm.to_mesh(mesh)
        bm.free()
        attribute = mesh.color_attributes.new(name="Col", type="FLOAT_COLOR", domain="CORNER")
        for poly in mesh.polygons:
            for loop_index in poly.loop_indices:
                attribute.data[loop_index].color = colors[poly.index]
        obj = bpy.data.objects.new(layer, mesh)
        bpy.context.collection.objects.link(obj)
        obj.data.materials.append(material(layer, *SURFACE[layer]))
        bevel = obj.modifiers.new("Bevel", "BEVEL")
        bevel.width = 0.07
        bevel.segments = 3
        bevel.limit_method = "NONE"
    # Stand the tile up like a badge, slightly turned, pivoting on its centre.
    pivot = bpy.data.objects.new("Pivot", None)
    pivot.location = (16, -16, 0)
    bpy.context.collection.objects.link(pivot)
    for layer in layers:
        obj = bpy.data.objects[layer]
        obj.parent = pivot
        obj.location = (-16, 16, 0)
    pivot.rotation_euler = (math.radians(72), 0, math.radians(-18))
    pivot.location = (16, -16, 15)
    return w, h


def area_light(name, location, energy, color, size):
    data = bpy.data.lights.new(name, "AREA")
    data.energy = energy
    data.color = color
    data.size = size
    obj = bpy.data.objects.new(name, data)
    obj.location = location
    bpy.context.collection.objects.link(obj)
    point_at(obj, (16, -16, 15))


def point_at(obj, target):
    direction = [t - o for t, o in zip(target, obj.location)]
    length = math.sqrt(sum(d * d for d in direction))
    dx, dy, dz = (d / length for d in direction)
    obj.rotation_euler = (math.atan2(math.hypot(dx, dy), -dz), 0, math.atan2(dy, dx) - math.pi / 2)


def camera():
    data = bpy.data.cameras.new("Camera")
    data.lens = 78
    cam = bpy.data.objects.new("Camera", data)
    cam.location = (16 + 22, -16 - 92, 22)
    bpy.context.collection.objects.link(cam)
    point_at(cam, (16, -16, 15))
    bpy.context.scene.camera = cam


scene = reset_scene()
build_voxels()
area_light("Key", (-25, -60, 45), 22000, (1.0, 0.96, 0.9), 30)
area_light("Top", (16, -30, 70), 9000, (0.85, 0.97, 1.0), 30)
area_light("Fill", (60, -45, 15), 4000, (0.5, 0.85, 1.0), 20)
area_light("Rim", (16, 25, 40), 9000, (1.0, 0.55, 0.6), 20)
camera()
scene.render.filepath = OUT
bpy.ops.render.render(write_still=True)
print("rendered", OUT)
