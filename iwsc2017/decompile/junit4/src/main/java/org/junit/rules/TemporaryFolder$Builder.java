package org.junit.rules;
import java.io.File;
public static class Builder {
    private File parentFolder;
    private boolean assureDeletion;
    public Builder parentFolder ( final File parentFolder ) {
        this.parentFolder = parentFolder;
        return this;
    }
    public Builder assureDeletion() {
        this.assureDeletion = true;
        return this;
    }
    public TemporaryFolder build() {
        return new TemporaryFolder ( this );
    }
}
