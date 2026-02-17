package one.armelin.igcmda.systems;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.adventure.memories.component.PlayerMemories;
import com.hypixel.hytale.builtin.adventure.memories.memories.npc.NPCMemory;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import it.unimi.dsi.fastutil.objects.ObjectList;
import one.armelin.igcmda.IGCMDA;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BeforeGatherMemoriesSystem extends EntityTickingSystem<EntityStore> {

    String lastMemory = "";

    public static final Query<EntityStore> QUERY = Query.and(
            TransformComponent.getComponentType(),
            Player.getComponentType(),
            PlayerMemories.getComponentType()
    );

    private final double radius;

    public BeforeGatherMemoriesSystem() {
        this.radius = 10.0;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        Player playerComponent = archetypeChunk.getComponent(index, Player.getComponentType());
        if (playerComponent == null) return;

        if (playerComponent.getGameMode() != GameMode.Adventure) return;

        TransformComponent transformComponent =
                archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        if (transformComponent == null) return;

        Vector3d position = transformComponent.getPosition();

        SpatialResource<Ref<EntityStore>, EntityStore> npcSpatialResource =
                store.getResource(NPCPlugin.get().getNpcSpatialResource());

        ObjectList<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        results.clear();

        npcSpatialResource.getSpatialStructure().collect(position, radius, results);

        if (results.isEmpty()) return;

        PlayerMemories playerMemories =
                archetypeChunk.getComponent(index, PlayerMemories.getComponentType());
        if (playerMemories == null) return;

        for (Ref<EntityStore> npcRef : results) {
            NPCEntity npc = commandBuffer.getComponent(npcRef, NPCEntity.getComponentType());
            if (npc == null) continue;

            Role role = npc.getRole();

            if (role == null || !role.isMemory()) continue;

            String npcRole = role.isMemoriesNameOverriden()
                    ? role.getMemoriesNameOverride()
                    : npc.getRoleName();

            String titleKey = role.getNameTranslationKey();

            NPCMemory temp = new NPCMemory(npcRole, titleKey);
            if (!MemoriesPlugin.get().hasRecordedMemory(temp)) {
                if (!playerMemories.getRecordedMemories().contains(temp)) {
                    String playerName = playerComponent.getDisplayName();
                    String memoryName = Message.translation(temp.getTitle()).getAnsiMessage();
                    if(lastMemory.equals(playerName + ":" + memoryName)) {
                        continue;
                    }
                    Map<String, Message> params = new HashMap<>();
                    params.put("{playername}", Message.raw(playerName).color(IGCMDA.config.colors.playerNameColor));
                    params.put("{memory}", Message.raw(memoryName).color(IGCMDA.config.colors.memoryColor));

                    Message finalMessage = formatTemplate(
                            IGCMDA.config.texts.memoryMessage,
                            IGCMDA.config.colors.messageColor,
                            params
                    );

                    IGCMDA.universe.sendMessage(finalMessage);
                    lastMemory = playerName + ":" + memoryName;
                }
            }
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public Set<Dependency<EntityStore>> getDependencies() {
        return Set.of(new SystemDependency<>(Order.BEFORE, NPCMemory.GatherMemoriesSystem.class));
    }

    public static Message formatTemplate(String template, String baseColor, Map<String, Message> params) {
        Message result = Message.raw("");
        int currentPos = 0;

        while (currentPos < template. length()) {
            int nextPos = -1;
            String foundKey = null;

            for (String key : params.keySet()) {
                int pos = template.indexOf(key, currentPos);
                if (pos != -1 && (nextPos == -1 || pos < nextPos)) {
                    nextPos = pos;
                    foundKey = key;
                }
            }

            if (nextPos == -1) {
                String remaining = template.substring(currentPos);
                if (!remaining.isEmpty()) {
                    result.insert(Message.raw(remaining).color(baseColor));
                }
                break;
            }

            if (nextPos > currentPos) {
                result.insert(Message.raw(template.substring(currentPos, nextPos)).color(baseColor));
            }

            result.insert(params.get(foundKey));
            currentPos = nextPos + foundKey. length();
        }

        return result;
    }
}


