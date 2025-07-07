package eu.pb4.polydecorations.datagen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record BlockbenchGroup(String name, List<Integer> children) {
    public static final Codec<List<BlockbenchGroup>> CODEC = Codec.either(RecordCodecBuilder.<BlockbenchGroup>create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(BlockbenchGroup::name),
            Codec.INT.listOf().fieldOf("children").forGetter(BlockbenchGroup::children)
    ).apply(instance, BlockbenchGroup::new)), Codec.INT).listOf()
            .xmap(x -> x.stream().filter(y -> y.left().isPresent()).map(Either::orThrow).toList(), x -> x.stream().map(Either::<BlockbenchGroup, Integer>left).toList()).fieldOf("groups").codec();
}
