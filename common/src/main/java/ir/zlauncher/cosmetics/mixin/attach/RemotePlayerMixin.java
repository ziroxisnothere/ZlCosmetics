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

package ir.zlauncher.cosmetics.mixin.attach;

import cc.cosmetica.core.api.Cosmetics;
import cc.cosmetica.core.impl.Logging;
import ir.zlauncher.cosmetics.StateHolder;
import cc.cosmetica.kupe.api.State;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RemotePlayer.class)
public abstract class RemotePlayerMixin extends AbstractClientPlayer implements StateHolder {
    public RemotePlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile, null);
    }

    @Unique
    private final State<Cosmetics> zlcosmetics$noCosmetics = new State<>(null);

    @Override
    public State<@Nullable Cosmetics> zlcosmetics$getCosmeticState() {
        if (Minecraft.getInstance().player == null)
            return zlcosmetics$noCosmetics;

        ClientPacketListener connection = Minecraft.getInstance().player.connection;
        PlayerInfo info = connection.getPlayerInfo(this.getUUID());

        if (info != null) {
            return ((StateHolder) info).zlcosmetics$getCosmeticState();
        } else {
            Logging.getInstance().warnOnce("remotePlayerInfoNullState", "Remote Player " + this.getUUID() + " had associated Player Info == null while retrieving state?");
            return zlcosmetics$noCosmetics;
        }
    }

    @Override
    public void zlcosmetics$setCosmeticState(@Nullable Cosmetics cosmetics) {
        if (Minecraft.getInstance().player == null)
            return;

        ClientPacketListener connection = Minecraft.getInstance().player.connection;
        PlayerInfo info = connection.getPlayerInfo(this.getUUID());

        if (info != null) {
            ((StateHolder) info).zlcosmetics$setCosmeticState(cosmetics);
        } else {
            Logging.getInstance().warnOnce("remotePlayerInfoNullState", "Remote Player " + this.getUUID() + " had associated Player Info == null while setting state?");
        }
    }
}
