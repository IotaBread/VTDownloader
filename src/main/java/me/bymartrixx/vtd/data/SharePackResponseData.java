package me.bymartrixx.vtd.data;

import org.jetbrains.annotations.Nullable;

public class SharePackResponseData {
    private final String result;
    @Nullable
    private final String code;

    public SharePackResponseData(String result) {
        this.result = result;
        this.code = null;
    }

    public SharePackResponseData(String result, @Nullable String code) {
        this.result = result;
        this.code = code;
    }

    public String getResult() {
        return this.result;
    }

    @Nullable
    public String getCode() {
        return this.code;
    }
}
