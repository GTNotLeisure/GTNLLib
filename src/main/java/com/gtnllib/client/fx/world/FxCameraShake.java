package com.gtnllib.client.fx.world;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.gtnewhorizon.gtnhlib.eventbus.Phase;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client camera shake. Each feature owns a {@link Source} and feeds it kicks, rumbles or a sustained tremor; the
 * camera shakes by the sum of all sources. Yaw and pitch are offset for one frame and restored after it, so the
 * player's real view direction never changes.
 */
@SideOnly(Side.CLIENT)
@EventBusSubscriber(side = Side.CLIENT, phase = Phase.PRE)
public class FxCameraShake {

    public enum Axis {

        YAW(1F, 6.3F, 11.7F, 17.9F),
        PITCH(0.85F, 7.1F, 13.3F, 19.7F),
        ROLL(1.5F, 4.3F, 8.9F, 15.1F);

        private final float gain;
        private final float[] frequencies;

        Axis(float gain, float first, float second, float third) {
            this.gain = gain;
            this.frequencies = new float[] { first, second, third };
        }
    }

    /**
     * One feature's share of the shake. Strengths are in degrees. Create it once and keep it in a static field;
     * use it on the client thread only.
     */
    public static class Source {

        private float impulse, rumble, sustain, sustainTarget;

        public Source() {
            SOURCES.add(this);
        }

        /** Sharp hit that fades in about 0.4 s. Stronger kicks replace weaker ones. */
        public void kick(float degrees) {
            if (degrees > 0F && Float.isFinite(degrees)) impulse = Math.max(impulse, degrees);
        }

        /** Slow aftershock that fades in about 1 s. Stronger rumbles replace weaker ones. */
        public void rumble(float degrees) {
            if (degrees > 0F && Float.isFinite(degrees)) rumble = Math.max(rumble, degrees);
        }

        /** Steady tremor the source eases toward; set it every tick while it lasts and 0 to stop. */
        public void sustain(float degrees) {
            sustainTarget = degrees > 0F && Float.isFinite(degrees) ? degrees : 0F;
        }

        public void clear() {
            impulse = 0F;
            rumble = 0F;
            sustain = 0F;
            sustainTarget = 0F;
        }

        public float intensity(float partialTicks) {
            return impulse * (float) Math.exp(-IMPULSE_DECAY * partialTicks * 0.05F)
                + rumble * (float) Math.exp(-RUMBLE_DECAY * partialTicks * 0.05F)
                + sustain;
        }

        /** This source's own offset on one axis, for models that sway with it. */
        public float tremor(Axis axis, float partialTicks) {
            return wave(axis, partialTicks) * intensity(partialTicks);
        }

        private boolean idle() {
            return impulse <= 0F && rumble <= 0F && sustain <= 0F;
        }

        private void advance() {
            impulse = fade(impulse, IMPULSE_DECAY);
            rumble = fade(rumble, RUMBLE_DECAY);
            sustain += (sustainTarget - sustain) * (1F - (float) Math.exp(-SUSTAIN_RESPONSE * 0.05F));
            if (sustainTarget <= 0F && sustain < NEGLIGIBLE) sustain = 0F;
        }
    }

    private static final List<Source> SOURCES = new CopyOnWriteArrayList<>();
    private static final float[] WEIGHTS = { 0.46F, 0.33F, 0.21F };
    private static final float IMPULSE_DECAY = 7.5F;
    private static final float RUMBLE_DECAY = 2.6F;
    private static final float SUSTAIN_RESPONSE = 16F;
    private static final float NEGLIGIBLE = 0.004F;
    private static final float PHASE_WRAP = 4096F;

    private static float seconds;
    private static EntityPlayer shaken;
    private static float appliedYaw;
    private static float appliedPitch;

    public static float tremor(Axis axis, float partialTicks) {
        float amplitude = 0F;
        for (Source source : SOURCES) amplitude += source.intensity(partialTicks);
        return amplitude <= 0F ? 0F : wave(axis, partialTicks) * amplitude;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Minecraft.getMinecraft().thePlayer == null) {
            restore();
            for (Source source : SOURCES) source.clear();
            return;
        }
        seconds += 0.05F;
        if (seconds > PHASE_WRAP) seconds -= PHASE_WRAP;
        for (Source source : SOURCES) source.advance();
    }

    @SubscribeEvent
    public static void render(TickEvent.RenderTickEvent event) {
        restore();
        if (event.phase == TickEvent.Phase.START) apply(Minecraft.getMinecraft().timer.renderPartialTicks);
    }

    private static float wave(Axis axis, float partialTicks) {
        float time = seconds + partialTicks * 0.05F;
        float value = 0F;
        for (int index = 0; index < WEIGHTS.length; index++) {
            float frequency = axis.frequencies[index];
            double modulation = Math.sin(time * frequency * 0.31F + index * 1.7D);
            value += WEIGHTS[index] * (float) Math.sin(time * frequency + modulation);
        }
        return value * axis.gain;
    }

    private static float fade(float value, float decay) {
        value *= (float) Math.exp(-decay * 0.05F);
        return value < NEGLIGIBLE ? 0F : value;
    }

    private static void apply(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.theWorld == null) return;
        boolean idle = true;
        for (Source source : SOURCES) idle &= source.idle();
        if (idle) return;
        float yaw = tremor(Axis.YAW, partialTicks), pitch = tremor(Axis.PITCH, partialTicks);
        if (Math.abs(yaw) < 1.0E-4F && Math.abs(pitch) < 1.0E-4F) return;
        shaken = minecraft.thePlayer;
        appliedYaw = yaw;
        appliedPitch = pitch;
        shaken.rotationYaw += yaw;
        shaken.rotationPitch += pitch;
    }

    private static void restore() {
        if (shaken == null) return;
        shaken.rotationYaw -= appliedYaw;
        shaken.rotationPitch -= appliedPitch;
        shaken = null;
        appliedYaw = 0F;
        appliedPitch = 0F;
    }
}
