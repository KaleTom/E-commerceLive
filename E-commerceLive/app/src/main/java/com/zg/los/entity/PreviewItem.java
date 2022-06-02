package com.zg.los.entity;

import android.view.TextureView;

public class PreviewItem {
    public TextureView tv;
    public String streamId;

    public PreviewItem(TextureView tv, String streamId) {
        this.tv = tv;
        this.streamId = streamId;
    }
}
