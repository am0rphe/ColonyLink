/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Longs
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.renderer.Rect2i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 */
package appeng.client.gui.widgets;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.MathExpressionParser;
import appeng.client.gui.NumberEntryType;
import appeng.client.gui.Rects;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.ConfirmableTextField;
import appeng.client.gui.widgets.ValidationIcon;
import appeng.core.localization.GuiText;
import com.google.common.primitives.Longs;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class NumberEntryWidget
implements ICompositeWidget {
    private static final long[] STEPS_1000 = new long[]{1L, 10L, 100L, 1000L};
    private static final long[] STEPS_64 = new long[]{1L, 16L, 32L, 64L};
    private final Component[] components1000;
    private final Component[] components64;
    private static final Component PLUS = Component.literal((String)"+");
    private static final Component MINUS = Component.literal((String)"-");
    private static final int UNIT_PADDING = 3;
    private final int errorTextColor;
    private final int normalTextColor;
    private final ConfirmableTextField textField;
    private final DecimalFormat decimalFormat;
    private NumberEntryType type;
    private List<Button> buttons;
    private long minValue;
    private long maxValue = Long.MAX_VALUE;
    private ValidationIcon validationIcon;
    private Runnable onChange;
    private Runnable onConfirm;
    private boolean hideValidationIcon;
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);
    private Rect2i textFieldBounds = Rects.ZERO;
    private Point currentScreenOrigin = Point.ZERO;
    private List<Button> amountButtons = List.of();

    public NumberEntryWidget(ScreenStyle style, NumberEntryType type) {
        this.errorTextColor = style.getColor(PaletteColor.TEXTFIELD_ERROR).toARGB();
        this.normalTextColor = style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB();
        this.type = Objects.requireNonNull(type, "type");
        this.decimalFormat = new DecimalFormat("#.######", new DecimalFormatSymbols());
        this.decimalFormat.setParseBigDecimal(true);
        this.decimalFormat.setNegativePrefix("-");
        Font font = Minecraft.getInstance().font;
        Objects.requireNonNull(font);
        this.textField = new ConfirmableTextField(style, font, 0, 0, 0, 9);
        this.textField.setBordered(false);
        this.textField.setMaxLength(16);
        this.textField.setTextColor(this.normalTextColor);
        this.textField.setVisible(true);
        this.textField.setResponder(text -> {
            this.validate();
            if (this.onChange != null) {
                this.onChange.run();
            }
        });
        this.textField.setOnConfirm(() -> {
            if (this.onConfirm != null && this.getLongValue().isPresent()) {
                this.onConfirm.run();
            }
        });
        this.validate();
        this.components1000 = new Component[]{this.makeLabel(PLUS, 0, true), this.makeLabel(PLUS, 1, true), this.makeLabel(PLUS, 2, true), this.makeLabel(PLUS, 3, true), this.makeLabel(MINUS, 0, true), this.makeLabel(MINUS, 1, true), this.makeLabel(MINUS, 2, true), this.makeLabel(MINUS, 3, true)};
        this.components64 = new Component[]{this.makeLabel(PLUS, 0, false), this.makeLabel(PLUS, 1, false), this.makeLabel(PLUS, 2, false), this.makeLabel(PLUS, 3, false), this.makeLabel(MINUS, 0, false), this.makeLabel(MINUS, 1, false), this.makeLabel(MINUS, 2, false), this.makeLabel(MINUS, 3, false)};
    }

    public void setOnConfirm(Runnable callback) {
        this.onConfirm = callback;
    }

    public void setOnChange(Runnable callback) {
        this.onChange = callback;
    }

    public void setActive(boolean active) {
        this.textField.setEditable(active);
        this.buttons.forEach(b -> {
            b.active = active;
        });
    }

    public void setTextFieldBounds(Rect2i bounds) {
        this.textFieldBounds = bounds;
        this.textField.move(this.currentScreenOrigin.move(bounds.getX(), bounds.getY()));
        int unitWidth = 0;
        if (this.type.unit() != null) {
            unitWidth = Minecraft.getInstance().font.width(this.type.unit()) + 3;
        }
        this.textField.resize(bounds.getWidth() - unitWidth, bounds.getHeight());
    }

    public void setTextFieldStyle(WidgetStyle style) {
        int left = 0;
        if (style.getLeft() != null) {
            left = style.getLeft();
        }
        int top = 0;
        if (style.getTop() != null) {
            top = style.getTop();
        }
        this.setTextFieldBounds(new Rect2i(left, top, style.getWidth(), style.getHeight()));
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
        this.validate();
    }

    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
        this.validate();
    }

    @Override
    public void setPosition(Point position) {
        this.bounds = new Rect2i(position.getX(), position.getY(), this.bounds.getWidth(), this.bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        this.bounds = new Rect2i(this.bounds.getX(), this.bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return this.bounds;
    }

    @Override
    public void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, AEBaseScreen<?> screen) {
        int left = bounds.getX() + this.bounds.getX();
        int top = bounds.getY() + this.bounds.getY();
        ArrayList<Button> buttons = new ArrayList<Button>(9);
        buttons.add(new AE2Button(left, top, 22, 20, this.components1000[0], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? STEPS_64[0] : STEPS_1000[0])));
        buttons.add(new AE2Button(left + 28, top, 28, 20, this.components1000[1], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? STEPS_64[1] : STEPS_1000[1])));
        buttons.add(new AE2Button(left + 62, top, 32, 20, this.components1000[2], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? STEPS_64[2] : STEPS_1000[2])));
        buttons.add(new AE2Button(left + 100, top, 38, 20, this.components1000[3], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? STEPS_64[3] : STEPS_1000[3])));
        buttons.forEach(addWidget);
        this.currentScreenOrigin = Point.fromTopLeft(bounds);
        this.setTextFieldBounds(this.textFieldBounds);
        screen.setInitialFocus((GuiEventListener)this.textField);
        addWidget.accept((AbstractWidget)this.textField);
        buttons.add(new AE2Button(left, top + 42, 22, 20, this.components1000[4], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? -STEPS_64[0] : -STEPS_1000[0])));
        buttons.add(new AE2Button(left + 28, top + 42, 28, 20, this.components1000[5], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? -STEPS_64[1] : -STEPS_1000[1])));
        buttons.add(new AE2Button(left + 62, top + 42, 32, 20, this.components1000[6], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? -STEPS_64[2] : -STEPS_1000[2])));
        buttons.add(new AE2Button(left + 100, top + 42, 38, 20, this.components1000[7], btn -> this.addQty(NumberEntryWidget.hasShiftOrControlDown() ? -STEPS_64[3] : -STEPS_1000[3])));
        this.amountButtons = List.copyOf(buttons);
        if (!this.hideValidationIcon) {
            this.validationIcon = new ValidationIcon();
            this.validationIcon.setX(left + 104);
            this.validationIcon.setY(top + 27);
            buttons.add(this.validationIcon);
        }
        buttons.subList(4, buttons.size()).forEach(addWidget);
        this.buttons = buttons;
        this.validate();
    }

    @Override
    public void updateBeforeRender() {
        Component[] messages = NumberEntryWidget.hasShiftOrControlDown() ? this.components64 : this.components1000;
        for (int i = 0; i < this.amountButtons.size(); ++i) {
            this.amountButtons.get(i).setMessage(messages[i]);
        }
    }

    private static boolean hasShiftOrControlDown() {
        return Screen.hasShiftDown() || Screen.hasControlDown();
    }

    public boolean startsWithEquals() {
        return this.textField.getValue().startsWith("=");
    }

    public OptionalInt getIntValue() {
        OptionalLong value = this.getLongValue();
        if (value.isPresent()) {
            long longValue = value.getAsLong();
            if (longValue > Integer.MAX_VALUE) {
                return OptionalInt.empty();
            }
            return OptionalInt.of((int)longValue);
        }
        return OptionalInt.empty();
    }

    public OptionalLong getLongValue() {
        Optional<BigDecimal> internalValue = this.getValueInternal();
        if (internalValue.isEmpty()) {
            return OptionalLong.empty();
        }
        if (this.type.amountPerUnit() == 1 && internalValue.get().scale() > 0) {
            return OptionalLong.empty();
        }
        long externalValue = this.convertToExternalValue(internalValue.get());
        if (externalValue < this.minValue) {
            return OptionalLong.empty();
        }
        if (externalValue > this.maxValue) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(externalValue);
    }

    public void setLongValue(long value) {
        BigDecimal internalValue = this.convertToInternalValue(Longs.constrainToRange((long)value, (long)this.minValue, (long)this.maxValue));
        this.textField.setValue(this.decimalFormat.format(internalValue));
        this.textField.moveCursorToEnd(false);
        this.textField.setHighlightPos(0);
        this.validate();
    }

    private void addQty(long delta) {
        BigDecimal currentValue = this.getValueInternal().orElse(BigDecimal.ZERO);
        BigDecimal newValue = currentValue.add(BigDecimal.valueOf(delta));
        BigDecimal minimum = this.convertToInternalValue(this.minValue).setScale(0, RoundingMode.CEILING);
        BigDecimal maximum = this.convertToInternalValue(this.maxValue).setScale(0, RoundingMode.FLOOR);
        if (newValue.compareTo(minimum) < 0) {
            newValue = minimum;
        } else if (newValue.compareTo(maximum) > 0) {
            newValue = maximum;
        } else if (currentValue.compareTo(BigDecimal.ONE) == 0 && delta > 1L) {
            newValue = newValue.subtract(BigDecimal.ONE);
        }
        this.setValueInternal(newValue);
    }

    private Optional<BigDecimal> getValueInternal() {
        String textValue = this.textField.getValue();
        if (textValue.startsWith("=")) {
            textValue = textValue.substring(1);
        }
        return MathExpressionParser.parse(textValue, this.decimalFormat);
    }

    private boolean isNumber() {
        ParsePosition position = new ParsePosition(0);
        String textValue = this.textField.getValue().trim();
        this.decimalFormat.parse(textValue, position);
        return position.getErrorIndex() == -1 && position.getIndex() == textValue.length();
    }

    private void setValueInternal(BigDecimal value) {
        this.textField.setValue(this.decimalFormat.format(value));
    }

    private void validate() {
        ArrayList<MutableComponent> validationErrors = new ArrayList<MutableComponent>();
        ArrayList<Component> infoMessages = new ArrayList<Component>();
        Optional<BigDecimal> possibleValue = this.getValueInternal();
        if (possibleValue.isPresent()) {
            if (this.type.amountPerUnit() == 1 && possibleValue.get().scale() > 0) {
                validationErrors.add(GuiText.NumberNonInteger.text());
            } else {
                long value = this.convertToExternalValue(possibleValue.get());
                if (value < this.minValue) {
                    String formatted = this.decimalFormat.format(this.convertToInternalValue(this.minValue));
                    validationErrors.add(GuiText.NumberLessThanMinValue.text(formatted));
                } else if (value > this.maxValue) {
                    String formatted = this.decimalFormat.format(this.convertToInternalValue(this.maxValue));
                    validationErrors.add(GuiText.NumberGreaterThanMaxValue.text(formatted));
                } else if (!this.isNumber()) {
                    infoMessages.add((Component)Component.literal((String)("= " + this.decimalFormat.format(possibleValue.get()))));
                }
            }
        } else {
            validationErrors.add(GuiText.InvalidNumber.text());
        }
        boolean valid = validationErrors.isEmpty();
        ArrayList<Component> tooltip = valid ? infoMessages : validationErrors;
        this.textField.setTextColor(valid ? this.normalTextColor : this.errorTextColor);
        this.textField.setTooltipMessage(tooltip);
        if (this.validationIcon != null) {
            this.validationIcon.setValid(valid);
            this.validationIcon.setTooltip(tooltip);
        }
    }

    private Component makeLabel(Component prefix, int amountIndex, boolean useDecimalSteps) {
        return prefix.plainCopy().append(this.decimalFormat.format(useDecimalSteps ? STEPS_1000[amountIndex] : STEPS_64[amountIndex]));
    }

    public void setHideValidationIcon(boolean hideValidationIcon) {
        this.hideValidationIcon = hideValidationIcon;
    }

    public NumberEntryType getType() {
        return this.type;
    }

    public void setType(NumberEntryType type) {
        if (this.type == type) {
            return;
        }
        this.type = type;
        this.setTextFieldBounds(this.textFieldBounds);
        if (this.onChange != null) {
            this.onChange.run();
        }
        this.validate();
    }

    private long convertToExternalValue(BigDecimal internalValue) {
        BigDecimal multiplicand = BigDecimal.valueOf(this.type.amountPerUnit());
        BigDecimal value = internalValue.multiply(multiplicand, MathContext.DECIMAL128);
        value = value.setScale(0, RoundingMode.UP);
        return value.longValue();
    }

    private BigDecimal convertToInternalValue(long externalValue) {
        BigDecimal divisor = BigDecimal.valueOf(this.type.amountPerUnit());
        return BigDecimal.valueOf(externalValue).divide(divisor, MathContext.DECIMAL128);
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
        if (this.type.unit() != null) {
            Font font = Minecraft.getInstance().font;
            int x = bounds.getX() + this.textFieldBounds.getX() + this.textFieldBounds.getWidth() - font.width(this.type.unit());
            float f = bounds.getY() + this.textFieldBounds.getY();
            int n = this.textFieldBounds.getHeight();
            Objects.requireNonNull(font);
            int y = (int)(f + (float)(n - 9) / 2.0f + 1.0f);
            guiGraphics.drawString(font, this.type.unit(), x, y, ChatFormatting.DARK_GRAY.getColor().intValue(), false);
        }
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        if (this.textFieldBounds.contains(mousePos.getX(), mousePos.getY()) && this.getValueInternal().isPresent()) {
            if (delta < 0.0) {
                this.addQty(-1L);
            } else if (delta > 0.0) {
                this.addQty(1L);
            }
            return true;
        }
        return false;
    }
}

