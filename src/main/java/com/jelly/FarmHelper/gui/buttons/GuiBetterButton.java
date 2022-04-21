package com.jelly.FarmHelper.gui.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class GuiBetterButton extends GuiButton {
    int x; int y; int widthln; int length; String buttonText;

    public GuiBetterButton(int buttonId, int x, int y, int widthln, int length, String buttonText) {
        super(buttonId, x, y, widthln, length, buttonText);
        this.x = x;
        this.y = y;
        this.widthln = widthln;
        this.length = length;
        this.buttonText = buttonText;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.hovered = (mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height);
            drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, new Color(255, 80, 80, 255).getRGB());
            this.mouseDragged(mc, mouseX, mouseY);
            int color = 14737632;
            if (packedFGColour != 0)
            {
                color = packedFGColour;
            }
            else if (!this.enabled)
            {
                color = 0xffffff;
            }
            else if (this.hovered)
            {
                color = 0xffffff;
            }
            this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, color);
        }

    }
}
