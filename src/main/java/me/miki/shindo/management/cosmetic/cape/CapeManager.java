package me.miki.shindo.management.cosmetic.cape;

import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.cosmetic.cape.impl.Cape;
import me.miki.shindo.management.cosmetic.cape.impl.CustomCape;
import me.miki.shindo.management.cosmetic.cape.impl.NormalCape;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.roles.ClientRole;
import me.miki.shindo.management.roles.ClientRoleManager;
import me.miki.shindo.utils.ImageUtils;
import me.miki.shindo.utils.file.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CapeManager {

    private final Minecraft mc = Minecraft.getMinecraft();

    private final ArrayList<Cape> capes = new ArrayList<Cape>();
    private Cape currentCape;

    public CapeManager() {

        Shindo instance = Shindo.getInstance();
        FileManager fileManager = instance.getFileManager();
        File customCapeDir = fileManager.getCustomCapeDir();
        File cacheDir = fileManager.getCapeCacheDir();

        capes.add(new NormalCape("None", null, null, CapeCategory.ALL, ClientRole.MEMBER));

        add("Minecon 2011", "minecon/2011-sample.png", "minecon/2011.png", CapeCategory.MINECON, ClientRole.MEMBER);
        add("Minecon 2012", "minecon/2012-sample.png", "minecon/2012.png", CapeCategory.MINECON, ClientRole.MEMBER);
        add("Minecon 2013", "minecon/2013-sample.png", "minecon/2013.png", CapeCategory.MINECON, ClientRole.MEMBER);
        add("Minecon 2015", "minecon/2015-sample.png", "minecon/2015.png", CapeCategory.MINECON, ClientRole.MEMBER);
        add("Minecon 2016", "minecon/2016-sample.png", "minecon/2016.png", CapeCategory.MINECON, ClientRole.MEMBER);

        add("Canada", "flag/canada-sample.png", "flag/canada.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("commonwealth", "flag/commonwealth-sample.png", "flag/commonwealth.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("England", "flag/england-sample.png", "flag/england.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Europe", "flag/europe-sample.png", "flag/europe.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("France", "flag/france-sample.png", "flag/france.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Germany", "flag/germany-sample.png", "flag/germany.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("India", "flag/india-sample.png", "flag/india.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Indonesia", "flag/indonesia-sample.png", "flag/indonesia.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Italy", "flag/italy-sample.png", "flag/italy.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Japan", "flag/japan-sample.png", "flag/japan.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Korea", "flag/korean-sample.png", "flag/korean.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("LGBT", "flag/lgbt-sample.png", "flag/lgbt.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("NATO", "flag/nato-sample.png", "flag/nato.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Scotland", "flag/scotland-sample.png", "flag/scotland.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Trans", "flag/trans-sample.png", "flag/trans.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("Ukraine", "flag/ukraine-sample.png", "flag/ukraine.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("UN", "flag/un-sample.png", "flag/un.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("United Kingdom", "flag/united-kingdom-sample.png", "flag/united-kingdom.png", CapeCategory.FLAG, ClientRole.GOLD);
        add("United States", "flag/united-states-sample.png", "flag/united-states.png", CapeCategory.FLAG, ClientRole.GOLD);

        //add("Blue", "shindo/blue-sample.png", "shindo/blue.png", CapeCategory.SOAR);
        //add("Orange", "shindo/orange-sample.png", "shindo/orange.png", CapeCategory.SOAR);
        //add("Terminal", "shindo/terminal-sample.png", "shindo/terminal.png", CapeCategory.SOAR);
        //add("Candy", "shindo/candy-sample.png", "shindo/candy.png", CapeCategory.SOAR);
        //add("Candy Floss", "shindo/candyfloss-sample.png", "shindo/candyfloss.png", CapeCategory.SOAR);
        //add("Northern Lights", "shindo/northenlights-sample.png", "shindo/northenlights.png", CapeCategory.SOAR);
        //add("Ocean", "shindo/ocean-sample.png", "shindo/ocean.png", CapeCategory.SOAR);
        //add("Parrot", "shindo/parrot-sample.png", "shindo/parrot.png", CapeCategory.SOAR);
        //add("Skylight", "shindo/skylight-sample.png", "shindo/skylight.png", CapeCategory.SOAR);
        //add("Sour Apple", "shindo/sourapple-sample.png", "shindo/sourapple.png", CapeCategory.SOAR);

        add("Aurora", "cartoon/aurora-sample.png", "cartoon/aurora.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Beach Girl", "cartoon/beachgirl-sample.png", "cartoon/beachgirl.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Beach Hut", "cartoon/beachhut-sample.png", "cartoon/beachhut.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Bridgeend", "cartoon/bridgeend-sample.png", "cartoon/bridgeend.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Cat", "cartoon/cat-sample.png", "cartoon/cat.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Cyber Cat", "cartoon/cybercat-sample.png", "cartoon/cybercat.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Decayed", "cartoon/decayed-sample.png", "cartoon/decayed.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Kitty", "cartoon/kitty-sample.png", "cartoon/kitty.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Lost World", "cartoon/lostworld-sample.png", "cartoon/lostworld.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Mountain", "cartoon/mountain-sample.png", "cartoon/mountain.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Stargazing Girl", "cartoon/stargazinggirl-sample.png", "cartoon/stargazinggirl.png", CapeCategory.CARTOON, ClientRole.DIAMOND);
        add("Stellagate", "cartoon/stellagate-sample.png", "cartoon/stellagate.png", CapeCategory.CARTOON, ClientRole.DIAMOND);

        //add("BreadCat", "misc/breadcat-sample.png", "misc/breadcat.png", CapeCategory.MISC);
        //add("Horse", "misc/horse-sample.png", "misc/horse.png", CapeCategory.MISC);
        //add("Trans Arch", "misc/transarch-sample.png", "misc/transarch.png", CapeCategory.MISC);

        currentCape = getCapeByName(InternalSettingsMod.getInstance().getCapeConfigName());

        for (File f : customCapeDir.listFiles()) {

            if (FileUtils.isImageFile(f)) {

                File file = new File(cacheDir, f.getName() + ".png");

                if (!file.exists()) {

                    try {
                        BufferedImage image = ImageIO.read(f);
                        int width = image.getWidth();
                        int height = image.getHeight();

                        BufferedImage outputImage = ImageUtils.scissor(image, (int) (width * 0.03125), (int) (height * 0.0625), (int) (width * 0.125), (int) (height * 0.46875));

                        ImageIO.write(ImageUtils.resize(outputImage, 1000, 1700), "png", file);
                    } catch (IOException e) {
                        ShindoLogger.error("Failed to load image", e);
                        continue;
                    }
                }

                if (file.exists()) {

                    try {
                        DynamicTexture cape = new DynamicTexture(ImageIO.read(f));

                        addCustomCape(f.getName().replace("." + FileUtils.getExtension(f), ""), file,
                                mc.getTextureManager().getDynamicTextureLocation(String.valueOf(f.getName().hashCode()), cape), CapeCategory.CUSTOM, ClientRole.DIAMOND);
                    } catch (Exception e) {
                        ShindoLogger.error("Failed to load image", e);
                    }
                }
            }
        }

        for (Cape c : capes) {

            if (c instanceof NormalCape) {

                NormalCape cape = (NormalCape) c;

                if (cape.getSample() != null) {
                    instance.getNanoVGManager().loadImage(cape.getSample());
                }
            }

            if (c instanceof CustomCape) {

                CustomCape cape = (CustomCape) c;

                if (cape.getSample() != null) {
                    instance.getNanoVGManager().loadImage(cape.getSample());
                }
            }
            if (c.getCape() != null) {
                mc.getTextureManager().bindTexture(c.getCape());
            }
        }
    }

    private void add(String name, String samplePath, String capePath, CapeCategory category, ClientRole requiredRole) {

        String cosmeticPath = "shindo/cosmetics/cape/";

        capes.add(new NormalCape(name, new ResourceLocation(cosmeticPath + samplePath), new ResourceLocation(cosmeticPath + capePath), category, requiredRole));
    }

    private void addCustomCape(String name, File sample, ResourceLocation cape, CapeCategory category, ClientRole requiredRole) {
        capes.add(new CustomCape(name, sample, cape, category, requiredRole));
    }

    public ArrayList<Cape> getCapes() {
        return capes;
    }

    public Cape getCurrentCape() {
        return currentCape;
    }

    public void setCurrentCape(Cape currentCape) {
        this.currentCape = currentCape;
        InternalSettingsMod.getInstance().setCapeConfigName(currentCape.getName());
    }

    public Cape getCapeByName(String name) {

        for (Cape c : capes) {
            if (c.getName().equals(name)) {
                return c;
            }
        }

        return getCapeByName("None");
    }

    public boolean canUseCape(UUID uuid, Cape cape) {
        return ClientRoleManager.hasRole(uuid, cape.getRequiredRole());
    }

    public TranslateText getTranslateError(ClientRole role) {
        switch (role) {
            case STAFF:
                return TranslateText.STAFF_ONLY;
            case DIAMOND:
                return TranslateText.DIAMOND_ONLY;
            case GOLD:
                return TranslateText.GOLD_ONLY;
            default:
                return TranslateText.NONE;

        }
    }

    public TranslateText getTranslateText(ClientRole role) {
        switch (role) {
            case STAFF:
                return TranslateText.STAFF;
            case DIAMOND:
                return TranslateText.DIAMOND;
            case GOLD:
                return TranslateText.GOLD;
            case MEMBER:
                return TranslateText.MEMBER;
            default:
                return TranslateText.NONE;

        }
    }
}
