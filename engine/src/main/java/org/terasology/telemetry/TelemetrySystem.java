/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.telemetry;

import com.snowplowanalytics.snowplow.tracker.emitter.Emitter;
import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.telemetry.metrics.ModulesMetric;
import org.terasology.world.block.BlockComponent;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * This component system is used to track metrics in game.
 */
@RegisterSystem
public class TelemetrySystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private final String trackerNamespace = this.getClass().toString();

    private GamePlayStatsComponent gamePlayStatsComponent;

    private Instant gameStartTime;

    private Vector3f previousPos;

    @In
    private Emitter emitter;

    @In
    private Metrics metrics;

    @In
    private Config config;

    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer localPlayer;

    public void update(float delta) {
        if (localPlayer.isValid()) {

            // the first frame when the local player is valid
            if (previousPos == null) {
                setGamePlayStatsComponent();
                previousPos = localPlayer.getPosition();
            } else {
                recordDistanceTraveled();
                recordPlayTime();
            }
        }
    }

    private void setGamePlayStatsComponent() {
        EntityRef localPlayerEntity = localPlayer.getCharacterEntity();
        if (!localPlayerEntity.hasComponent(GamePlayStatsComponent.class)) {
            gamePlayStatsComponent = new GamePlayStatsComponent();
            localPlayerEntity.addOrSaveComponent(gamePlayStatsComponent);
        } else {
            gamePlayStatsComponent = localPlayerEntity.getComponent(GamePlayStatsComponent.class);
        }
    }

    private void recordDistanceTraveled() {
        Vector3f position = localPlayer.getPosition();
        float distanceMoved = position.distance(previousPos);
        gamePlayStatsComponent.distanceTraveled += distanceMoved;
        previousPos = position;
        localPlayer.getCharacterEntity().addOrSaveComponent(gamePlayStatsComponent);
    }

    private void recordPlayTime() {
        long playTime = Duration.between(gameStartTime, Instant.now()).toMinutes();
        gamePlayStatsComponent.playTimeMinute = playTime;
        localPlayer.getCharacterEntity().addOrSaveComponent(gamePlayStatsComponent);

    }

    @Override
    public void postBegin() {
        sendMetric();

        gameStartTime = Instant.now();
    }

    private void sendMetric() {
        if (config.getTelemetryConfig().isTelemetryEnabled()) {
            sendModuleMetric();
            sendSystemContextMetric();
        }
    }

    private void sendModuleMetric() {
        ModulesMetric modulesMetric = metrics.getModulesMetric();
        Unstructured unstructuredMetric = modulesMetric.getUnstructuredMetric();
        TelemetryUtils.trackMetric(emitter, trackerNamespace, unstructuredMetric);
    }

    private void sendSystemContextMetric() {
        Unstructured systemContextMetric = metrics.getSystemContextMetric().getUnstructuredMetric();
        TelemetryUtils.trackMetric(emitter, trackerNamespace, systemContextMetric);
    }
}
