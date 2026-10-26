package com.example.autominesetpoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AutoMineSetPointClient implements ClientModInitializer {
    private static KeyBinding setPos1Key;
    private static KeyBinding setPos2Key;
    private static KeyBinding toggleKey;

    private static BlockPos pos1;
    private static BlockPos pos2;
    private static boolean enabled = false;
    private static BlockPos currentTarget = null;

    @Override
    public void onInitializeClient() {
        setPos1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.automine_setpoint.set_pos1", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J, KeyBinding.Category.MISC));
        setPos2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.automine_setpoint.set_pos2", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K, KeyBinding.Category.MISC));
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.automine_setpoint.toggle", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B, KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
    }

    private static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null || client.interactionManager == null) return;

        while (setPos1Key.wasPressed()) {
            BlockPos target = getLookedAtBlock(client);
            if (target != null) {
                pos1 = target.toImmutable();
                client.player.sendMessage(Text.literal("AutoMine: SetPoint 1 = " + pos1.toShortString()), false);
            } else {
                client.player.sendMessage(Text.literal("Nhìn vào block rồi bấm J để đặt điểm 1."), false);
            }
        }
        while (setPos2Key.wasPressed()) {
            BlockPos target = getLookedAtBlock(client);
            if (target != null) {
                pos2 = target.toImmutable();
                client.player.sendMessage(Text.literal("AutoMine: SetPoint 2 = " + pos2.toShortString()), false);
            } else {
                client.player.sendMessage(Text.literal("Nhìn vào block rồi bấm K để đặt điểm 2."), false);
            }
        }
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            currentTarget = null;
            client.player.sendMessage(Text.literal("AutoMine: " + (enabled ? "BẬT" : "TẮT")), false);
        }

        if (!enabled || pos1 == null || pos2 == null) return;

        BlockPos next = findNextBlock(client);
        if (next == null) {
            currentTarget = null;
            return;
        }

        if (!next.equals(currentTarget)) {
            currentTarget = next.toImmutable();
            client.interactionManager.attackBlock(currentTarget, Direction.UP);
        }
        client.interactionManager.updateBlockBreakingProgress(currentTarget, Direction.UP);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    private static BlockPos getLookedAtBlock(MinecraftClient client) {
        if (client.crosshairTarget instanceof BlockHitResult hit) {
            return hit.getBlockPos();
        }
        return null;
    }

    private static BlockPos findNextBlock(MinecraftClient client) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Keep the scan small to avoid freezing the client on huge selections.
        if ((long)(maxX-minX+1) * (maxY-minY+1) * (maxZ-minZ+1) > 4096) return null;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Vec3d eye = client.player.getEyePos();
        double reach = 5.0;
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    BlockState state = client.world.getBlockState(mutable);
                    if (state.isAir() || state.getHardness(client.world, mutable) < 0) continue;
                    double distance = eye.squaredDistanceTo(Vec3d.ofCenter(mutable));
                    if (distance <= reach * reach && distance < bestDistance) {
                        bestDistance = distance;
                        best = mutable.toImmutable();
                    }
                }
            }
        }
        return best;
    }
}
