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
package org.terasology.telemetry.metrics;

import com.snowplowanalytics.snowplow.tracker.events.Unstructured;
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson;
import org.terasology.config.Config;
import org.terasology.config.PlayerConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.telemetry.TelemetryCategory;
import org.terasology.telemetry.TelemetryField;
import org.terasology.world.generator.WorldGenerator;

import java.util.Locale;
import java.util.Map;

/**
 */
@TelemetryCategory(id = "gameConfiguration",
        displayName = "${engine:menu#telemetry-game-configuration}"
)
public class GameConfigurationMetric extends Metric {

    public static final String SCHEMA_GAME_CONFIGURATION = "iglu:org.terasology/gameConfiguration/jsonschema/1-0-0";

    @TelemetryField
    private SimpleUri worldGenerator;

    @TelemetryField
    private NetworkMode networkMode;

    @TelemetryField
    private Locale language;

    @TelemetryField
    private float playerHeight;

    @TelemetryField
    private float playerEyeHeight;

    private Context context;

    public GameConfigurationMetric(Context context) {
        this.context = context;
    }

    @Override
    public Unstructured getUnstructuredMetric() {
        Map<String, ?> metricMap = getFieldValueMap();
        SelfDescribingJson modulesData = new SelfDescribingJson(SCHEMA_GAME_CONFIGURATION, metricMap);

        return Unstructured.builder()
                .eventData(modulesData)
                .build();
    }

    @Override
    public Map<String, ?> getFieldValueMap() {
        fetchWorldGenerator();
        fetchNetworkMode();
        fetchConfig();

        return super.getFieldValueMap();
    }

    private void fetchWorldGenerator() {
        WorldGenerator generator = CoreRegistry.get(WorldGenerator.class);
        if (generator != null) {
            worldGenerator = generator.getUri();
        }
    }

    private void fetchNetworkMode() {
        NetworkSystem networkSystem = context.get(NetworkSystem.class);
        networkMode = networkSystem.getMode();
    }

    private void fetchConfig() {
        Config config = context.get(Config.class);
        language = config.getSystem().getLocale();

        PlayerConfig playerConfig = config.getPlayer();
        playerHeight = playerConfig.getHeight();
        playerEyeHeight = playerConfig.getEyeHeight();
    }
}
