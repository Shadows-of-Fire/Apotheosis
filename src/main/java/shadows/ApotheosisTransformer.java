package shadows;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import shadows.anvil.asm.AnvilTransformer;
import shadows.ench.asm.EnchTransformer;
import shadows.potion.asm.PotionTransformer;
import shadows.spawn.asm.SpawnerTransformer;

@SortingIndex(1001)
public class ApotheosisTransformer implements IClassTransformer {

	public List<IApotheosisTransformer> transformers = new LinkedList<>();

	public ApotheosisTransformer() {
		transformers.add(new AnvilTransformer());
		transformers.add(new EnchTransformer());
		transformers.add(new PotionTransformer());
		transformers.add(new SpawnerTransformer());
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		for (IApotheosisTransformer t : transformers)
			if (t.accepts(name, transformedName)) return t.transform(name, transformedName, basicClass);
		return basicClass;
	}

	public static interface IApotheosisTransformer extends IClassTransformer {
		boolean accepts(String name, String transformedName);
	}

}
