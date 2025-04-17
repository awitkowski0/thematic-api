package com.funalex.themAnim.impl;

import com.funalex.themAnim.core.impl.AnimationProcessor;
import com.funalex.themAnim.core.util.SetableSupplier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface IMutableModel {

    void setEmoteSupplier(SetableSupplier<AnimationProcessor> emoteSupplier);

    SetableSupplier<AnimationProcessor> getEmoteSupplier();

}
