/**
 * Copyright (C) 2016 eBusiness Information
 *
 * This file is part of OSM Contributor.
 *
 * OSM Contributor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSM Contributor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSM Contributor.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.mapsquare.osmcontributor.rest.managers;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.mapsquare.osmcontributor.model.events.ResetTypeDatabaseEvent;
import io.mapsquare.osmcontributor.rest.clients.H2GeoPresetsRestClient;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoDto;
import io.mapsquare.osmcontributor.rest.dtos.dma.H2GeoPresetsDto;
import io.mapsquare.osmcontributor.rest.events.PresetDownloadedEvent;
import io.mapsquare.osmcontributor.rest.events.PresetListDownloadedEvent;
import io.mapsquare.osmcontributor.rest.events.error.PresetDownloadErrorEvent;
import io.mapsquare.osmcontributor.rest.events.error.PresetListDownloadErrorEvent;
import io.mapsquare.osmcontributor.rest.mappers.H2GeoPresetsMapper;
import io.mapsquare.osmcontributor.ui.events.presets.PleaseDownloadPresetEvent;
import io.mapsquare.osmcontributor.ui.events.presets.PleaseDownloadPresetListEvent;
import retrofit.RetrofitError;
import timber.log.Timber;

@Singleton
public class H2GeoPresetsManager {

  private final H2GeoPresetsRestClient presetsRestClient;
  private final EventBus bus;
  private final H2GeoPresetsMapper presetsMapper;

  @Inject
  public H2GeoPresetsManager(H2GeoPresetsRestClient presetsRestClient, EventBus bus,
      H2GeoPresetsMapper presetsMapper) {
    this.presetsRestClient = presetsRestClient;
    this.bus = bus;
    this.presetsMapper = presetsMapper;
  }

  // ********************************
  // ************ Events ************
  // ********************************

  @Subscribe(threadMode = ThreadMode.ASYNC)
  public void onPleaseDownloadPresetListEvent(PleaseDownloadPresetListEvent event) {
    Timber.d("Requesting preset list download");
    try {
      H2GeoPresetsDto presets = presetsRestClient.loadProfiles();
      bus.post(new PresetListDownloadedEvent(presetsMapper.convertToH2GeoPresets(presets)));
    } catch (RetrofitError error) {
      Timber.e(error, "Retrofit error, couldn't download preset list");
      bus.post(new PresetListDownloadErrorEvent());
    }
  }

  @Subscribe(threadMode = ThreadMode.ASYNC)
  public void onPleaseDownloadPresetEvent(PleaseDownloadPresetEvent event) {
    String filename = event.getFilename();
    Timber.d("Requesting preset '%s' download", filename);
    try {
      H2GeoDto h2GeoDto = presetsRestClient.loadProfile(filename);
      bus.post(new ResetTypeDatabaseEvent(h2GeoDto));
      bus.post(new PresetDownloadedEvent(h2GeoDto));
    } catch (RetrofitError error) {
      Timber.e(error, "Retrofit error, couldn't download preset '%s'", filename);
      bus.post(new PresetDownloadErrorEvent(filename));
    }
  }
}
