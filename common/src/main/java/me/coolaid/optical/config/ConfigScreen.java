package me.coolaid.optical.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConfigScreen {
    private static final List<Integer> LIMITS = List.of(30, 60, 90, 360);

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("optical.config.title"))

                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("optical.category.freelook"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("optical.freelook.option.enabled"))
                                .description(OptionDescription.of(Component.translatable("optical.freelook.option.enabled.desc")))
                                .binding(true, OpticalConfig.FREELOOK::isEnabled, OpticalConfig.FREELOOK::setEnabled)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("optical.freelook.option.toggle_mode"))
                                .description(OptionDescription.of(Component.translatable("optical.freelook.option.toggle_mode.desc")))
                                .binding(false, OpticalConfig.FREELOOK::isToggleMode, OpticalConfig.FREELOOK::setToggleMode)
                                .controller(opt -> BooleanControllerBuilder.create(opt)
                                        .formatValue(val -> val ? Component.translatable("optical.freelook.value.toggle") : Component.translatable("optical.freelook.value.hold")))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("optical.freelook.option.invert"))
                                .description(OptionDescription.of(Component.translatable("optical.freelook.option.invert.desc")))
                                .binding(true, OpticalConfig.FREELOOK::isInvertY, OpticalConfig.FREELOOK::setInvertY)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Double>createBuilder()
                                .name(Component.translatable("optical.freelook.option.sensitivity"))
                                .description(OptionDescription.of(Component.translatable("optical.freelook.option.sensitivity.desc")))
                                .binding(1.0D, OpticalConfig.FREELOOK::getSensitivityMultiplier, OpticalConfig.FREELOOK::setSensitivityMultiplier)
                                .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.1D, 2.0D).step(0.05D).formatValue(val -> Component.literal(String.format("%.2fx", val))))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("optical.freelook.option.limit"))
                                .description(OptionDescription.of(Component.translatable("optical.freelook.option.limit.desc")))
                                .binding(3, () -> {
                                    int index = LIMITS.indexOf(OpticalConfig.FREELOOK.getRotationLimit());
                                    return index != -1 ? index : 3;
                                }, index -> OpticalConfig.FREELOOK.setRotationLimit(LIMITS.get(index)))
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(0, LIMITS.size() - 1)
                                        .step(1)
                                        .formatValue(index -> {
                                            int value = LIMITS.get(index);
                                            return Component.literal(value == 360 ? "360°" : value + "°");
                                        }))
                                .build())
                        .option(Option.<OpticalConfig.FreelookConfig.Style>createBuilder()
                                .name(Component.translatable("optical.freelook.option.style"))
                                .description(OptionDescription.of(Component.translatable("optical.freelook.option.style.desc")))
                                .binding(OpticalConfig.FreelookConfig.Style.CLASSIC, OpticalConfig.FREELOOK::getStyle, OpticalConfig.FREELOOK::setStyle)
                                .controller(opt -> EnumControllerBuilder.create(opt)
                                        .enumClass(OpticalConfig.FreelookConfig.Style.class)
                                        .formatValue(val -> Component.translatable("optical.value.style." + val.name().toLowerCase())))
                                .build())
                        .build())

                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("optical.category.brightness"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("optical.brightness.option.enabled"))
                                .description(OptionDescription.of(Component.translatable("optical.brightness.option.enabled.desc")))
                                .binding(true, OpticalConfig.BRIGHTNESS::isEnabled, OpticalConfig.BRIGHTNESS::setEnabled)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("optical.brightness.option.toggled_state"))
                                .description(OptionDescription.of(Component.translatable("optical.brightness.option.toggled_state.desc")))
                                .binding(false, OpticalConfig.BRIGHTNESS::isToggled, OpticalConfig.BRIGHTNESS::setToggled)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("optical.brightness.option.default"))
                                .description(OptionDescription.of(Component.translatable("optical.brightness.option.default.desc")))
                                .binding(100, OpticalConfig.BRIGHTNESS::getDefaultLevel, OpticalConfig.BRIGHTNESS::setDefaultLevel)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.translatable("optical.brightness.option.toggled"))
                                .description(OptionDescription.of(Component.translatable("optical.brightness.option.toggled.desc")))
                                .binding(1500, OpticalConfig.BRIGHTNESS::getToggledLevel, OpticalConfig.BRIGHTNESS::setToggledLevel)
                                .controller(IntegerFieldControllerBuilder::create)
                                .build())
                        .build())

                // Empty Categories
                .category(ConfigCategory.createBuilder().name(Component.translatable("optical.category.general")).build())
                .category(ConfigCategory.createBuilder().name(Component.translatable("optical.category.freecam")).build())
                .category(ConfigCategory.createBuilder().name(Component.translatable("optical.category.zoom")).build())

                .build()
                .generateScreen(parent);
    }
}