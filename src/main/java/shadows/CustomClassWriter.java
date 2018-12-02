package shadows;

import java.net.URLClassLoader;

import org.objectweb.asm.ClassWriter;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class CustomClassWriter extends ClassWriter {
	public static URLClassLoader customClassLoader = new URLClassLoader(((URLClassLoader) Launch.classLoader.getClass().getClassLoader()).getURLs());

	public CustomClassWriter(int flags) {
		super(flags);
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		type1 = FMLDeobfuscatingRemapper.INSTANCE.unmap(type1);
		type2 = FMLDeobfuscatingRemapper.INSTANCE.unmap(type2);

		if (type1.equals("java/lang/Object") || type2.equals("java/lang/Object")) { return "java/lang/Object"; }

		Class<?> c, d;
		ClassLoader classLoader = customClassLoader;
		try {
			c = Class.forName(type1.replace('/', '.'), false, classLoader);
			d = Class.forName(type2.replace('/', '.'), false, classLoader);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) { return type1; }
		if (d.isAssignableFrom(c)) { return type2; }
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));

			String result = FMLDeobfuscatingRemapper.INSTANCE.map(c.getName().replace('.', '/'));

			return result;
		}
	}
}