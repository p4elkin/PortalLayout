package org.vaadin.addon.portallayout.demo;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class DemoTable extends Table {
    
    public static enum NameType {
        NT_FIRST_NAME("First Name"),
        NT_LAST_NAME("Last Name");
        
        private String title;
        
        private NameType(String title) {
            this.title = title;
        }
        
        @Override
        public String toString() {
            return title;
        }
    }
    
    private IndexedContainer container;
    
    public DemoTable() {
        super();
        setWidth("100%");
        setHeight("200px");
        populateContainer();
    }

    private void populateContainer() {
        container = new IndexedContainer();
        container.addContainerProperty("firstName", String.class, "");
        container.addContainerProperty("lastName", String.class, "");
        addRecord("Ian", "Curtis");
        addRecord("Kurt", "Cobain");
        addRecord("Keith", "Richards");
        addRecord("Mike", "Stipe");
        addRecord("Paul", "Banks");
        setContainerDataSource(container);
    }
    
    private void addRecord(String first, String last) {
        Item item = container.getItem(container.addItem());
        item.getItemProperty("firstName").setValue(first);
        item.getItemProperty("lastName").setValue(last);
    }
    
    public void filter(final NameType type, final String value) {
        container.removeAllContainerFilters();
        switch (type) {
        case NT_FIRST_NAME:
            container.addContainerFilter("firstName", value, true, false);
            break;
        case NT_LAST_NAME:
            container.addContainerFilter("lastName", value, true, false);
            break;
        }
    }
}
