package org.apache.tomcat.util.digester;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
public class RulesBase implements Rules {
    protected HashMap<String, List<Rule>> cache = new HashMap<>();
    protected Digester digester = null;
    protected String namespaceURI = null;
    protected ArrayList<Rule> rules = new ArrayList<>();
    @Override
    public Digester getDigester() {
        return ( this.digester );
    }
    @Override
    public void setDigester ( Digester digester ) {
        this.digester = digester;
        Iterator<Rule> items = rules.iterator();
        while ( items.hasNext() ) {
            Rule item = items.next();
            item.setDigester ( digester );
        }
    }
    @Override
    public String getNamespaceURI() {
        return ( this.namespaceURI );
    }
    @Override
    public void setNamespaceURI ( String namespaceURI ) {
        this.namespaceURI = namespaceURI;
    }
    @Override
    public void add ( String pattern, Rule rule ) {
        int patternLength = pattern.length();
        if ( patternLength > 1 && pattern.endsWith ( "/" ) ) {
            pattern = pattern.substring ( 0, patternLength - 1 );
        }
        List<Rule> list = cache.get ( pattern );
        if ( list == null ) {
            list = new ArrayList<>();
            cache.put ( pattern, list );
        }
        list.add ( rule );
        rules.add ( rule );
        if ( this.digester != null ) {
            rule.setDigester ( this.digester );
        }
        if ( this.namespaceURI != null ) {
            rule.setNamespaceURI ( this.namespaceURI );
        }
    }
    @Override
    public void clear() {
        cache.clear();
        rules.clear();
    }
    @Override
    public List<Rule> match ( String namespaceURI, String pattern ) {
        List<Rule> rulesList = lookup ( namespaceURI, pattern );
        if ( ( rulesList == null ) || ( rulesList.size() < 1 ) ) {
            String longKey = "";
            Iterator<String> keys = this.cache.keySet().iterator();
            while ( keys.hasNext() ) {
                String key = keys.next();
                if ( key.startsWith ( "*/" ) ) {
                    if ( pattern.equals ( key.substring ( 2 ) ) ||
                            pattern.endsWith ( key.substring ( 1 ) ) ) {
                        if ( key.length() > longKey.length() ) {
                            rulesList = lookup ( namespaceURI, key );
                            longKey = key;
                        }
                    }
                }
            }
        }
        if ( rulesList == null ) {
            rulesList = new ArrayList<>();
        }
        return ( rulesList );
    }
    @Override
    public List<Rule> rules() {
        return ( this.rules );
    }
    protected List<Rule> lookup ( String namespaceURI, String pattern ) {
        List<Rule> list = this.cache.get ( pattern );
        if ( list == null ) {
            return ( null );
        }
        if ( ( namespaceURI == null ) || ( namespaceURI.length() == 0 ) ) {
            return ( list );
        }
        ArrayList<Rule> results = new ArrayList<>();
        Iterator<Rule> items = list.iterator();
        while ( items.hasNext() ) {
            Rule item = items.next();
            if ( ( namespaceURI.equals ( item.getNamespaceURI() ) ) ||
                    ( item.getNamespaceURI() == null ) ) {
                results.add ( item );
            }
        }
        return ( results );
    }
}
