package org.jfree.data.general;
public interface Dataset {
    void addChangeListener ( DatasetChangeListener p0 );
    void removeChangeListener ( DatasetChangeListener p0 );
    DatasetGroup getGroup();
    void setGroup ( DatasetGroup p0 );
}
