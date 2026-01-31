package myau.util;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class Reflection {
    // Mapa para não precisar buscar o campo via reflexão toda vez (performance)
    private static final HashMap<FieldData, Field> fieldMap = new HashMap<>();

    // Classe auxiliar para identificar o campo no mapa
    private static final class FieldData {
        private final Class<?> aClass;
        private final String field;

        public FieldData(Class<?> aClass, String field) {
            this.aClass = aClass;
            this.field = field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldData fieldData = (FieldData) o;
            return aClass.equals(fieldData.aClass) && field.equals(fieldData.field);
        }

        @Override
        public int hashCode() {
            return 31 * aClass.hashCode() + field.hashCode();
        }
    }

    // Método que busca o campo, torna acessível e remove o 'final' se necessário
    private static @NotNull Field getField(@NotNull FieldData data) {
        if (!fieldMap.containsKey(data)) {
            try {
                // Tenta pegar o campo pelo nome SRG ou nome real
                Field target = data.aClass.getDeclaredField(data.field);
                target.setAccessible(true);

                // Remove o modificador 'final' se existir para permitir manipulação total
                int modifiers = target.getModifiers();
                if (Modifier.isFinal(modifiers)) {
                    try {
                        Field modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                        modifiersField.setInt(target, modifiers & ~Modifier.FINAL);
                    } catch (NoSuchFieldException ignored) {}
                }

                fieldMap.put(data, target);
                return target;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Erro ao acessar campo: " + data.field, e);
            }
        }
        return fieldMap.get(data);
    }

    // O método que o seu getTimer chama
    public static Object get(@NotNull Object object, @NotNull String field) {
        final FieldData data = new FieldData(object.getClass(), field);
        try {
            return getField(data).get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Versão com cast automático (Timer.class)
    public static <T> T get(@NotNull Object object, @NotNull String field, @NotNull Class<T> type) {
        return type.cast(get(object, field));
    }
}