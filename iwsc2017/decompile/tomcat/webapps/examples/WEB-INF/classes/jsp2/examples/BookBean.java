// 
// Decompiled by Procyon v0.5.29
// 

package jsp2.examples;

public class BookBean
{
    private final String title;
    private final String author;
    private final String isbn;
    
    public BookBean(final String title, final String author, final String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public String getAuthor() {
        return this.author;
    }
    
    public String getIsbn() {
        return this.isbn;
    }
}
