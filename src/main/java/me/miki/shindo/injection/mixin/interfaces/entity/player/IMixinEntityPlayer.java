package me.miki.shindo.injection.mixin.interfaces.entity.player;

import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples;
import me.miki.shindo.management.mods.impl.skin3d.render.CustomizableModelPart;

/**
 * Interface de accessor para injetar funcionalidades customizadas em EntityPlayer
 */
public interface IMixinEntityPlayer {
    
    /**
     * Obtém as camadas de skin customizáveis
     */
    CustomizableModelPart[] getSkinLayers();
    
    /**
     * Configura as camadas de skin customizáveis
     */
    void setupSkinLayers(CustomizableModelPart[] box);
    
    /**
     * Obtém as camadas de cabeça customizáveis
     */
    CustomizableModelPart getHeadLayers();
    
    /**
     * Configura as camadas de cabeça customizáveis
     */
    void setupHeadLayers(CustomizableModelPart box);
    
    /**
     * Obtém as amostras de dados do jogador para detecção de cheats
     */
    PlayerDataSamples getPlayerDataSamples();
    
    /**
     * Configura as amostras de dados do jogador para detecção de cheats
     */
    void setPlayerDataSamples(PlayerDataSamples data);
}
