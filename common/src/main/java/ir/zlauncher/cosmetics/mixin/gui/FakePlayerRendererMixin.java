/*
 * Copyright 2024, 2025 Cosmetica
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ir.zlauncher.cosmetics.mixin.gui;

import cc.cosmetica.core.api.Accessory;
import cc.cosmetica.core.api.CachedImage;
import cc.cosmetica.core.impl.NametagRenderer;
import ir.zlauncher.cosmetics.gui.player.AccessoriesAttachment;
import ir.zlauncher.cosmetics.gui.widget.RotatableGUIPlayer;
import ir.zlauncher.cosmetics.util.NametagUtil;
import cc.cosmetica.kupe.api.Canvas;
import cc.cosmetica.kupe.api.Context;
import cc.cosmetica.kupe.api.Text;
import cc.cosmetica.kupe.api.gui.GUIPlayer;
import cc.cosmetica.kupe.impl.fakeplayer.FakePlayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(value = FakePlayerRenderer.class)
public class FakePlayerRendererMixin {
    @Shadow public List<GUIPlayer.Nametag> nametags;

    @Unique private @Nullable CachedImage zlcosmetics$icon0 = null;
    @Unique private boolean zlcosmetics$iconTransparent = false;
    @Unique private @Nullable CachedImage zlcosmetics$icon1 = null;

    @Inject(at = @At("HEAD"), method = "drawLivingEntity", remap = false)
    private void onDrawLiving(GUIPlayer player, Context context, float rotation, float delta, PoseStack stack,
                              MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        if (player instanceof RotatableGUIPlayer) {
            // don't need to reset to null provided one renderer per gui player.
            this.zlcosmetics$icon0 = ((RotatableGUIPlayer)player).icon;
            this.zlcosmetics$iconTransparent = ((RotatableGUIPlayer) player).hasTransparentIcon();
            this.zlcosmetics$icon1 = ((RotatableGUIPlayer)player).loreIcon;
        }
    }

    @Inject(at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
            ordinal = 1,
            shift = At.Shift.AFTER),
            method = "drawLivingEntity")
    private void onStartDrawNametag(GUIPlayer player, Context context, float rotation, float delta, PoseStack stack,
                              MultiBufferSource bufferSource, int light, CallbackInfo ci) {
        int nametags = this.nametags.size();
        if (nametags > 1) {
            Text lore = this.nametags.get(1).text;
            if (lore.isEmpty()) {
                nametags--;
            }
        }
        NametagUtil.shiftNametags(stack, player, nametags);
    }

    @Inject(at = @At("HEAD"), method = "renderNametag", remap = false)
    private void onRenderNametag(GUIPlayer.Nametag nametag, Canvas canvas, MultiBufferSource bufferSource,
                                 int packedLight, CallbackInfo ci) {
        if (nametag == nametags.get(0)) {
            if (zlcosmetics$icon0 != null) {
                NametagRenderer.prepareIcon(zlcosmetics$icon0, 2, this.zlcosmetics$iconTransparent, true);
            }
        } else if (nametags.size() > 1 && nametag == nametags.get(1)) {
            if (zlcosmetics$icon1 != null) {
                NametagRenderer.prepareIcon(zlcosmetics$icon1, 2, false, true);
            }
        }
    }
}
