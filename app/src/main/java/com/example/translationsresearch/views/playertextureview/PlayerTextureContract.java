/*
 * PlayerTextureContract.java
 * webka
 *
 * Copyright (C) 2019, Realtime Technologies Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the
 * property of Realtime Technologies Limited and its SUPPLIERS, if any.
 *
 * The intellectual and technical concepts contained herein are
 * proprietary to Realtime Technologies Limited and its suppliers and
 * may be covered by Russian Federation and Foreign Patents, patents
 * in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Realtime Technologies Limited.
 */

package com.example.translationsresearch.views.playertextureview;

import androidx.annotation.Keep;

import java.util.Optional;

import reactor.core.publisher.Flux;

/**
 * @author Konstantin Epifanov
 * @since 02.08.2019
 */
@Keep public abstract class PlayerTextureContract {

  final Flux<Optional<String>> mFluxItems;
  final Flux<Boolean> mFluxActivated, mFluxChecked;

  PlayerTextureContract(Flux<Optional<String>> fluxItems, Flux<Boolean> fluxActivated, Flux<Boolean> fluxChecked) {
    mFluxItems = fluxItems;
    mFluxActivated = fluxActivated;
    mFluxChecked = fluxChecked;
  }

  abstract PlayerTextureView getView();

}