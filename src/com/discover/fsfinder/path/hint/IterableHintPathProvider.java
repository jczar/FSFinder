package com.discover.fsfinder.path.hint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.discover.fsfinder.path.FSPathProvider;
import com.discover.fsfinder.path.IterablePathProvider;

public class IterableHintPathProvider
    implements FSPathProvider, IterablePathProvider {

    private Collection<FSPathProvider> hintProviders;
    
    public List<String> getPaths() {
        Set<String> hints = new HashSet<String>();
        
        if (null == hintProviders)
            return null;
        
        for (FSPathProvider provider: hintProviders) {
            List<String> hintsList = provider.getPaths();
            
            if (null != hintsList)
                hints.addAll(hintsList);
        }

        return new ArrayList<String>(hints);
    }

    public void setProviderCollection(Collection<FSPathProvider> hintProviders) {
        this.hintProviders = hintProviders;
    }
}
