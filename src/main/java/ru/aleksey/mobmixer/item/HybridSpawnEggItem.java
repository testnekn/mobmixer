package ru.aleksey.mobmixer.item;

import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import ru.aleksey.mobmixer.hybrid.HybridMobFactory;
import ru.aleksey.mobmixer.hybrid.HybridSpawnData;

public class HybridSpawnEggItem extends Item {
    public HybridSpawnEggItem() {
        super(new Settings().maxCount(16));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (!HybridSpawnData.hasParents(stack)) {
            return super.getName(stack);
        }

        return Text.translatable(
            "item.mobmixer.hybrid_spawn_egg.named",
            HybridSpawnData.getFirstParentName(stack),
            HybridSpawnData.getSecondParentName(stack)
        );
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var world = context.getWorld();
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        EntityType<?> firstParent = HybridSpawnData.getFirstParent(context.getStack());
        EntityType<?> secondParent = HybridSpawnData.getSecondParent(context.getStack());
        if (firstParent == null || secondParent == null || !(world instanceof ServerWorld serverWorld)) {
            return ActionResult.FAIL;
        }

        BlockPos spawnPos = context.getBlockPos().offset(context.getSide());
        MobEntity hybrid = HybridMobFactory.createHybrid(serverWorld, spawnPos, firstParent, secondParent);
        if (hybrid == null) {
            return ActionResult.FAIL;
        }

        serverWorld.spawnEntityAndPassengers(hybrid);
        serverWorld.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, spawnPos);
        context.getStack().decrement(1);
        return ActionResult.CONSUME;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.mobmixer.hybrid_spawn_egg.tooltip_1").formatted(Formatting.GRAY));
        if (HybridSpawnData.hasParents(stack)) {
            tooltip.add(Text.translatable("item.mobmixer.hybrid_spawn_egg.parent_1", HybridSpawnData.getFirstParentName(stack)).formatted(Formatting.DARK_AQUA));
            tooltip.add(Text.translatable("item.mobmixer.hybrid_spawn_egg.parent_2", HybridSpawnData.getSecondParentName(stack)).formatted(Formatting.DARK_AQUA));
        }
        tooltip.add(Text.translatable("item.mobmixer.hybrid_spawn_egg.tooltip_2").formatted(Formatting.DARK_GRAY));
    }
}
