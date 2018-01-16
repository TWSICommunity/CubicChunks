package cubicchunks.worldgen.gui.component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import cubicchunks.worldgen.gui.ExtraGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class UIList<E, C extends UIComponent<?>> extends UILayout<UIList<E, C>> {

    private final List<E> data;
    private final Function<? super E, ? extends C> componentFactory;
    private final BiMap<E, C> componentMap = HashBiMap.create(30);

    /**
     * Default constructor, creates the components list.
     *
     * The data argument is not copied and modifying it's content will directly modify the layout.
     * After any external change to the data (including reordering), {@link #setNeedsLayoutUpdate} must be called.
     *
     * @param gui the gui
     */
    public UIList(ExtraGui gui, List<E> data, Function<? super E, ? extends C> componentFactory) {
        super(gui);
        this.data = data;
        this.componentFactory = componentFactory;
    }

    public Collection<E> getData() {
        return data;
    }

    public void remove(E element) {
        remove(componentMap.remove(element));
        data.remove(element);
    }

    public void add(E element) {
        data.add(element);
    }

    public void replace(E previous, E next) {
        int idx = data.indexOf(previous);
        if (idx < 0) {
            data.add(next);
        } else {
            data.set(idx, next);
        }
        componentMap.remove(previous);
    }

    @Override
    public void add(UIComponent<?>... components) {
        for (UIComponent<?> component : components) {
            if (!componentMap.containsValue(component)) {
                throw new UnsupportedOperationException();
            }
        }
        super.add(components);

    }

    @Override
    public void remove(UIComponent<?> component) {
        super.remove(component);
        this.data.remove(componentMap.inverse().get(component));
    }

    @Override protected void layout() {
        cleanupComponentMap();
        doLayout();
    }

    private void cleanupComponentMap() {
        if (componentMap.keySet().retainAll(new HashSet<>(data))) {
            Set<UIComponent<?>> toRemove = new HashSet<>();
            for (UIComponent<?> c : this.components) {
                if (!componentMap.containsValue(c)) {
                    toRemove.add(c);
                }
            }
            toRemove.forEach(this::remove);
        }
    }

    private void doLayout() {
        int y = 0;
        for (E e : data) {
            C c = component(e);
            if (c.getX() != 0 || c.getY() != y) {
                c.setPosition(0, y);
            }
            y += c.getHeight();
        }
        this.contentHeight = y;
    }

    public C component(E e) {
        if (!componentMap.containsKey(e)) {
            componentMap.put(e, this.componentFactory.apply(e));
            add(componentMap.get(e));
        }
        return componentMap.get(e);
    }

    @Override protected boolean isLayoutChanged() {
        return true;
    }
}
