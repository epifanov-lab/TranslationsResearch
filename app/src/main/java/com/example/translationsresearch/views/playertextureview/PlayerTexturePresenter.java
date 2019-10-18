/*
 * PlayerTexturePresenter.java
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

import android.view.TextureView;

import com.webka.sdk.players.WebkaPlayer;
import com.webka.sdk.schedulers.Schedulers;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater;
import static java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater;


/**
 * @author Konstantin Epifanov
 * @since 02.08.2019
 */
public class PlayerTexturePresenter implements Disposable {

  private final static long
    CHECKED = 1L,
    NON_CHECKED = 0L;

  private volatile long mChecked = NON_CHECKED;

  private static final AtomicLongFieldUpdater<PlayerTexturePresenter>
    CHECKED_UPDATER = newUpdater(PlayerTexturePresenter.class, "mChecked");

  private final Disposable dActivate;
  private final Disposable dChecked;
  private PlayerTextureContract mContract;

  /** Socket updater. */
  private static final AtomicReferenceFieldUpdater<PlayerTexturePresenter, WebkaPlayer>
    PLAYER_UPDATER = newUpdater(PlayerTexturePresenter.class, WebkaPlayer.class, "mWebkaPlayer");

  private volatile WebkaPlayer mWebkaPlayer = WebkaPlayer.EMPTY;

  PlayerTexturePresenter(PlayerTextureContract contract, BiFunction<String, TextureView, Mono<WebkaPlayer>> webkaPlayerSupplier) {
    mContract = contract;

    dActivate = mContract.mFluxActivated
      .flatMap(isActivated -> isActivated ? mContract.mFluxItems : Flux.never())
      .doOnNext(s -> setPlayer(WebkaPlayer.EMPTY))
      .flatMap(Mono::justOrEmpty)
      .switchMap(url -> webkaPlayerSupplier.apply(url, mContract.getView()))
      .subscribe(this::setPlayer);

    dChecked = mContract.mFluxChecked
      .subscribe(this::setChecked);
  }

  private void setPlayer(WebkaPlayer player) {
    WebkaPlayer previous;
    do if ((previous = getPlayer()) == player) return;
    while (!PLAYER_UPDATER.compareAndSet(this, previous, player));
    previous.dispose();
    invalidate (player, getChecked());
  }

  private void setChecked(boolean value) {
    Schedulers.trowIfNotMainThread();
    final long checked = value ? CHECKED : NON_CHECKED;
    long previous;
    do if ((previous = getChecked()) == checked) return;
    while (!CHECKED_UPDATER.compareAndSet(this, previous, checked));
    invalidate (getPlayer(), checked);
  }

  private WebkaPlayer getPlayer() {
    return PLAYER_UPDATER.get(this);
  }

  private long getChecked() {
    return CHECKED_UPDATER.get(this);
  }

  private void invalidate(WebkaPlayer player, long checked) {
    if (player != WebkaPlayer.EMPTY) player.setPlayWhenReady(checked == CHECKED);
  }

  private boolean isPlayerAvailable() {
    return getPlayer() != WebkaPlayer.EMPTY;
  }

  @Override
  public void dispose() {
    setPlayer(WebkaPlayer.EMPTY);
    dChecked.dispose();
    dActivate.dispose();
  }

  @Override
  public boolean isDisposed() {
    return dActivate.isDisposed();
  }

}