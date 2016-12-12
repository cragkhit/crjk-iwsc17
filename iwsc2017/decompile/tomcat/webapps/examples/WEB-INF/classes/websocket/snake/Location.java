// 
// Decompiled by Procyon v0.5.29
// 

package websocket.snake;

public class Location
{
    public int x;
    public int y;
    
    public Location(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
    
    public Location getAdjacentLocation(final Direction direction) {
        switch (direction) {
            case NORTH: {
                return new Location(this.x, this.y - 10);
            }
            case SOUTH: {
                return new Location(this.x, this.y + 10);
            }
            case EAST: {
                return new Location(this.x + 10, this.y);
            }
            case WEST: {
                return new Location(this.x - 10, this.y);
            }
            default: {
                return this;
            }
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Location location = (Location)o;
        return this.x == location.x && this.y == location.y;
    }
    
    @Override
    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.y;
        return result;
    }
}
