package dev.shadowsoffire.apotheosis.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.datafixers.util.Either;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SimpleTexButton extends Button {

    public static final WidgetSprites APOTH_SPRITES = new WidgetSprites(
        Apotheosis.loc("widget/button"),
        Apotheosis.loc("widget/button_disabled"),
        Apotheosis.loc("widget/button_highlighted"));

    protected final Either<Identifier, WidgetSprites> texture;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int textureWidth;
    protected final int textureHeight;
    protected List<Component> inactiveMessage = List.of();
    protected BiConsumer<SimpleTexButton, Consumer<Component>> tooltipProvider = (btn, consumer) -> {};
    protected Component buttonText = CommonComponents.EMPTY;
    protected boolean forceHovered = false;

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, Identifier texture, Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, 256, 256, pOnPress);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, Identifier texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, pTextureWidth, pTextureHeight, pOnPress, CommonComponents.EMPTY);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, Identifier texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Component pMessage) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, Either.left(texture), pTextureWidth, pTextureHeight, pOnPress, DEFAULT_NARRATION, pMessage);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, Either<Identifier, WidgetSprites> texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress,
        Button.CreateNarration pOnTooltip, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.texture = texture;
    }

    public SimpleTexButton setInactiveMessage(Component msg) {
        if (msg == CommonComponents.EMPTY) {
            this.inactiveMessage = List.of();
        }
        else {
            this.inactiveMessage = Arrays.asList(msg);
        }
        return this;
    }

    public SimpleTexButton setInactiveMessage(List<Component> msg) {
        this.inactiveMessage = msg;
        return this;
    }

    public SimpleTexButton setTooltipProvider(BiConsumer<SimpleTexButton, Consumer<Component>> provider) {
        this.tooltipProvider = provider;
        return this;
    }

    public SimpleTexButton setButtonText(Component msg) {
        this.buttonText = msg;
        return this;
    }

    @Override
    public void setPosition(int pX, int pY) {
        this.setX(pX);
        this.setY(pY);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor gfx, int pMouseX, int pMouseY, float pPartialTick) {
        int yTex = this.yTexStart;
        if (!this.isActive()) {
            yTex += this.height;
        }
        else if (this.isHovered() || this.forceHovered) {
            yTex += this.height * 2;
        }

        if (this.texture.left().isPresent()) {
            Identifier texture = this.texture.left().orElseThrow();
            gfx.blit(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.xTexStart, yTex, this.width, this.height, this.textureWidth, this.textureHeight);
        }
        else {
            WidgetSprites sprites = this.texture.right().orElseThrow();
            Identifier texture = sprites.get(this.isActive(), this.isHovered() || this.forceHovered);
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        }
        this.extractDefaultLabel(gfx.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
        if (this.isHovered()) {
            this.renderToolTip(gfx, pMouseX, pMouseY);
        }
    }

    @Override
    protected void extractDefaultLabel(ActiveTextCollector output) {
        if (this.buttonText == CommonComponents.EMPTY) {
            return;
        }
        Component message = this.buttonText;
        if (getFGColor() != UNSET_FG_COLOR) {
            final int fg = getFGColor();
            message = message.copy().withStyle(style -> style.withColor(fg));
        }
        this.extractScrollingStringOverContents(output, message, 2);
    }

    public void renderToolTip(GuiGraphicsExtractor gfx, int pMouseX, int pMouseY) {
        if (this.getMessage() != CommonComponents.EMPTY && this.isHovered()) {
            Component primary = this.getMessage();
            if (!this.active && primary.getStyle().getColor() == null) {
                primary = primary.copy().withStyle(ChatFormatting.GRAY);
            }
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(primary);
            if (this.active) {
                this.tooltipProvider.accept(this, tooltips::add);
            }
            else {
                tooltips.addAll(this.inactiveMessage);
            }
            gfx.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltips, pMouseX, pMouseY);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected int x = -1;
        protected int y = -1;
        protected int width = -1;
        protected int height = -1;
        protected int u = 0;
        protected int v = 0;
        protected int textureWidth = 256;
        protected int textureHeight = 256;
        protected Component message = CommonComponents.EMPTY;
        protected List<Component> inactiveMessage = new ArrayList<>();
        protected BiConsumer<SimpleTexButton, Consumer<Component>> provider = (btn, consumer) -> {};
        protected Component buttonText = CommonComponents.EMPTY;
        protected Either<Identifier, WidgetSprites> texture = null;
        protected OnPress action = btn -> {};

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder texPos(int u, int v) {
            this.u = u;
            this.v = v;
            return this;
        }

        public Builder texSize(int texWidth, int texHeight) {
            this.textureWidth = texWidth;
            this.textureHeight = texHeight;
            return this;
        }

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder inactiveMessage(Component message) {
            this.inactiveMessage.add(message);
            return this;
        }

        public Builder inactiveMessage(List<Component> message) {
            this.inactiveMessage = message;
            return this;
        }

        public Builder tooltipProvider(BiConsumer<SimpleTexButton, Consumer<Component>> provider) {
            this.provider = provider;
            return this;
        }

        public Builder buttonText(Component message) {
            this.buttonText = message;
            return this;
        }

        public Builder texture(Identifier texture) {
            this.texture = Either.left(texture);
            return this;
        }

        public Builder texture(WidgetSprites texture) {
            this.texture = Either.right(texture);
            return this;
        }

        public Builder action(OnPress action) {
            this.action = action;
            return this;
        }

        public SimpleTexButton build() {
            Preconditions.checkArgument(this.width >= 0 && this.height >= 0, "Size must be set");
            Preconditions.checkNotNull(this.texture, "Texture must bet set");
            return new SimpleTexButton(this.x, this.y, this.width, this.height, this.u, this.v, this.texture, this.textureWidth, this.textureHeight, action, DEFAULT_NARRATION, message)
                .setInactiveMessage(this.inactiveMessage)
                .setTooltipProvider(this.provider)
                .setButtonText(this.buttonText);
        }
    }

}
