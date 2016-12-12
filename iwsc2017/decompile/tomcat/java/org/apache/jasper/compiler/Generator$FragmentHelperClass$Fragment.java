package org.apache.jasper.compiler;
private static class Fragment {
    private GenBuffer genBuffer;
    private int id;
    public Fragment ( final int id, final Node node ) {
        this.id = id;
        this.genBuffer = new GenBuffer ( null, node.getBody() );
    }
    public GenBuffer getGenBuffer() {
        return this.genBuffer;
    }
    public int getId() {
        return this.id;
    }
}
