package com.bonsai.pixelpets.client.gui.screens;

import com.bonsai.pixelpets.PixelPets;
import com.bonsai.pixelpets.entities.AbstractPixelPetEntity;
import com.bonsai.pixelpets.mixin.ScreenAccessor;
import com.bonsai.pixelpets.networking.packets.client.ServerboundRequestEquipPetPacket;
import com.bonsai.pixelpets.pixelpets.PixelPet;
import com.bonsai.pixelpets.pixelpets.PixelPetsInventory;
import com.bonsai.pixelpets.pixelpets.PlayerPetAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class PixelPetsInventoryScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(PixelPets.MOD_ID, "textures/gui/pixel_pets_gui.png");
    
    private static final boolean DEBUG_LAYOUT = false;
    private static final int GUI_TEXTURE_SIZE = 512;
    private static final int FAINTED_OVERLAY_COLOR = 0x99000000;
    private static final int TICKS_PER_MINUTE = 20 * 60;
    private static final long MILLIS_PER_TICK = 50L;
    
    private final Player player;
    private final PixelPetsInventory inventory;
    private final Map<PixelPet, LivingEntity> entities = new IdentityHashMap<>();
    
    private LivingEntity selectedPetEntity = null;
    protected int imageWidth = 338;
    protected int imageHeight = 180;
    protected int leftPos;
    protected int topPos;
    
    private PixelPet selectedPet = null;
    private int selectedSlotIndex = -1;
    private boolean isSelectedFromEquipped = false;
    
    public PixelPetsInventoryScreen(Component title) {
        super(title);
        this.player = Minecraft.getInstance().player;
        this.inventory = ((PlayerPetAccess) this.player).pixelPets$getPetInventory();
    }
    
    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }
    
    @Override
    public void removed() {
        super.removed();
        this.selectedPetEntity = null;
        this.entities.clear();
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.flush();
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        
        graphics.blit(GUI_TEXTURE, this.leftPos, this.topPos, 0, 0, imageWidth, 180, GUI_TEXTURE_SIZE, GUI_TEXTURE_SIZE);
        
        this.renderEquippedTeam(graphics, mouseX, mouseY);
        this.renderStorageGrid(graphics, mouseX, mouseY);
        
        if (this.selectedPet != null && !this.selectedPet.isEmpty()) {
            this.renderPetDetails(graphics, mouseX, mouseY);
        }
        
        if (DEBUG_LAYOUT) {
            this.renderDebugOverlays(graphics);
        }
        
        for (Renderable renderable : ((ScreenAccessor) this).getRenderables()) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
    }
    
    private void renderDebugOverlays(GuiGraphics graphics) {
        int teamStartX = this.leftPos + 6;
        int teamStartY = this.topPos + 25;
        for (int i = 0; i < 4; i++) {
            int yPos = teamStartY + (i * 39);
            graphics.fill(teamStartX, yPos, teamStartX + 91, yPos + 33, 0x6600AAFF);
        }
        
        int gridStartX = this.leftPos + 111;
        int gridStartY = this.topPos + 27;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 4; col++) {
                int xPos = gridStartX + (col * 30);
                int yPos = gridStartY + (row * 30);
                graphics.fill(xPos, yPos, xPos + 26, yPos + 26, 0x6600FF00);
            }
        }
        
        graphics.fill(this.leftPos + 243, this.topPos + 27, this.leftPos + 243 + 87, this.topPos + 27 + 87, 0x44FFAA00);
        graphics.fill(this.leftPos + 243, this.topPos + 158, this.leftPos + 243 + 44, this.topPos + 158 + 15, 0xAAFF00FF);
        graphics.fill(this.leftPos + 298, this.topPos + 158, this.leftPos + 298 + 15, this.topPos + 158 + 15, 0xAAFF3333);
        graphics.fill(this.leftPos + 315, this.topPos + 158, this.leftPos + 315 + 15, this.topPos + 158 + 15, 0x6600FF00);
    }
    
    private void renderEquippedTeam(GuiGraphics graphics, int mouseX, int mouseY) {
        int startX = this.leftPos + 6;
        int startY = this.topPos + 25;
        
        for (int i = 0; i < 4; i++) {
            PixelPet slotPet = this.inventory.equipped.get(i);
            int yPos = startY + (i * 39);
            
            if (slotPet.isEmpty()) continue;
            
            int cooldown = slotPet.getFaintCooldown();
            boolean isFainted = cooldown > 0;
            
            if (selectedSlotIndex == i && isSelectedFromEquipped && !DEBUG_LAYOUT) {
                graphics.fill(startX, yPos, startX + 91, yPos + 33, 0x33FFFFFF);
            }
            
            this.renderPetPortrait(graphics, slotPet, startX - 5, yPos - 7, 45, isFainted);
            
            String displayName = slotPet.getName();
            if (displayName.length() > 10) {
                displayName = displayName.substring(0, 8) + "...";
            }
            
            LivingEntity entity = this.getOrCreateAsEntity(slotPet);
            float health = entity == null || entity.getMaxHealth() <= 0.0F ? 1.0F : Mth.clamp(entity.getHealth() / entity.getMaxHealth(), 0.0F, 1.0F);

            graphics.drawString(this.font, displayName, startX + 34, yPos + 5, isFainted ? 0x8A8A8A : 0xFFFFFF);
            
            if (isFainted) {
                String timeText = String.format(Locale.ROOT, "%.1f m", (float) cooldown / TICKS_PER_MINUTE);
                graphics.drawString(this.font, timeText, startX + 34, yPos + 18, 0xE0E0E0);
                graphics.blit(GUI_TEXTURE, startX + 13, yPos + 12, 280, 0, 8, 8, GUI_TEXTURE_SIZE, GUI_TEXTURE_SIZE);
            } else {
                int barWidth = Math.round(49 * health);
                if (barWidth > 0) {
                    graphics.blit(GUI_TEXTURE, startX + 37, yPos + 19, 0, 194, barWidth, 10, GUI_TEXTURE_SIZE, GUI_TEXTURE_SIZE);
                }
            }
        }
    }
    
    private void renderStorageGrid(GuiGraphics graphics, int mouseX, int mouseY) {
        int startX = this.leftPos + 111;
        int startY = this.topPos + 27;
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 4; col++) {
                int index = (row * 4) + col;
                int xPos = startX + (col * 30);
                int yPos = startY + (row * 30);
                
                PixelPet slotPet = this.inventory.stored.get(index);
                
                if (this.selectedSlotIndex == index && !this.isSelectedFromEquipped && !DEBUG_LAYOUT) {
                    graphics.fill(xPos, yPos, xPos + 26, yPos + 26, 0x33FFFFFF);
                }
                
                if (!slotPet.isEmpty()) {
                    this.renderPetPortrait(graphics, slotPet, xPos + 1, yPos + 1, 24, slotPet.getFaintCooldown() > 0);
                }
            }
        }
    }
    
    private void renderPetDetails(GuiGraphics graphics, int mouseX, int mouseY) {
        int detailX = this.leftPos + 243;
        int detailY = this.topPos + 27;
        
//        graphics.drawString(this.font, "LVL: " + this.selectedPet.getLevel(), detailX + 5, detailY + 14, 0xFFFFFF); //TODO: lineup
        
        if (this.selectedPetEntity == null) return;
        
        this.tickPreviewEntity(this.selectedPetEntity, 0);

        float yawAngle = (float) ((System.currentTimeMillis() / 25.0) % 360L) * ((float) Math.PI / 180.0F);
        Quaternionf pose = new Quaternionf().rotationXYZ((float) Math.toRadians(15), yawAngle, (float) Math.toRadians(180));
        
        InventoryScreen.renderEntityInInventory(
            graphics, (float) (detailX + 43), (float) (detailY + 75), 95.0F,
            new Vector3f(0.0F, 0.0F, 0.0F), pose, null, this.selectedPetEntity
        );
    }
    
    public void updateSelectedPet(PixelPet pet, int slotIndex, boolean fromEquipped) {
        this.selectedPet = pet;
        this.selectedSlotIndex = slotIndex;
        this.isSelectedFromEquipped = fromEquipped;
        this.selectedPetEntity = this.createPetEntity(pet);
    }
    
    private LivingEntity createPetEntity(PixelPet pet) {
        if (pet == null || pet.isEmpty() || pet.getDataLocation() == null || pet.getDataLocation().getPath().isEmpty()) return null;
        
        try {
            EntityType<?> type = pet.getEntityType(this.player.registryAccess());
            if (type == null) return null;
            
            Entity entity = type.create(this.player.level());
            if (entity instanceof AbstractPixelPetEntity petEntity) {
                petEntity.initialize(pet.getDataLocation(), pet.getLevel(), this.inventory.petStatus);
            }
            
            return entity instanceof LivingEntity living ? living : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void tickPreviewEntity(LivingEntity entity, int phaseOffsetTicks) {
        entity.tickCount = (int) (System.currentTimeMillis() / MILLIS_PER_TICK) + phaseOffsetTicks;
    }
    
    private LivingEntity getOrCreateAsEntity(PixelPet pet) {
        return this.entities.computeIfAbsent(pet, this::createPetEntity);
    }
    
    private void flush() {
        if (this.entities.isEmpty()) return;
        
        Set<PixelPet> activePets = new HashSet<>();
        for (PixelPet pet : this.inventory.equipped) {
            if (!pet.isEmpty()) activePets.add(pet);
        }
        
        for (PixelPet pet : this.inventory.stored) {
            if (!pet.isEmpty()) activePets.add(pet);
        }
        
        this.entities.keySet().removeIf(cached -> !activePets.contains(cached));
    }
    
    private void renderPetPortrait(GuiGraphics graphics, PixelPet pet, int x, int y, int size, boolean grayscale) {
        LivingEntity portraitEntity = this.getOrCreateAsEntity(pet);
        
        if (portraitEntity == null) {
            graphics.fill(x, y, x + size, y + size, grayscale ? 0xFF555555 : 0xFFBF7038);
            return;
        }
        
        this.tickPreviewEntity(portraitEntity, Math.floorMod(System.identityHashCode(pet), 20));
        
        InventoryScreen.renderEntityInInventory(
            graphics, x + size / 2, y + size, (float) size,
            new Vector3f(0.0F, -0.20F, 0.0F),
            new Quaternionf().rotationXYZ((float) Math.toRadians(10), (float) Math.toRadians(225), (float) Math.toRadians(180)),
            null, portraitEntity
        );
        
        if (grayscale) {
            graphics.fill(x, y, x + size, y + size, FAINTED_OVERLAY_COLOR);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.checkEquippedClicks(mouseX, mouseY)) return true;
        if (this.checkStorageClicks(mouseX, mouseY)) return true;
        if (this.checkActionButtons(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private boolean checkEquippedClicks(double mouseX, double mouseY) {
        int startX = this.leftPos + 6;
        int startY = this.topPos + 25;
        
        for (int i = 0; i < 4; i++) {
            int yPos = startY + (i * 39);
            if (this.isClickInside(mouseX, mouseY, startX, yPos, 91, 33)) {
                this.updateSelectedPet(this.inventory.equipped.get(i), i, true);
                return true;
            }
        }
        return false;
    }
    
    private boolean checkStorageClicks(double mouseX, double mouseY) {
        int startX = this.leftPos + 111;
        int startY = this.topPos + 27;
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 4; col++) {
                int index = (row * 4) + col;
                int xPos = startX + (col * 30);
                int yPos = startY + (row * 30);
                
                if (this.isClickInside(mouseX, mouseY, xPos, yPos, 26, 26)) {
                    this.updateSelectedPet(this.inventory.stored.get(index), index, false);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean checkActionButtons(double mouseX, double mouseY) {
        if (this.selectedPet == null || this.selectedPet.isEmpty()) return false;
        
        if (this.isClickInside(mouseX, mouseY, this.leftPos + 243, this.topPos + 158, 44, 15)) {
            this.sendEquipPacket();
            return true;
        }
        
        if (this.isClickInside(mouseX, mouseY, this.leftPos + 298, this.topPos + 158, 15, 15)) {
            this.sendReleasePacket();
            return true;
        }
        
        return false;
    }
    
    private boolean isClickInside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
    
    private void sendEquipPacket() {
        if (this.selectedPet == null) return;
        ServerboundRequestEquipPetPacket.sendToServer(this.selectedSlotIndex, this.isSelectedFromEquipped);
        this.updateSelectedPet(null, -1, false);
    }
    
    private void sendReleasePacket() {
        if (this.selectedPet == null) return;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}