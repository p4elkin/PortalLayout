package org.vaadin.addon.portallayout.demo;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

import org.vaadin.addon.portallayout.PortalLayout;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.TextFileProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class DashBoardPanel extends HorizontalSplitPanel {

    private final PortalLayout sidePortal = new PortalLayout();

    private final PortalLayout bottomPortal = new PortalLayout();

    private PortalLayout mainPortal = new PortalLayout();

    private HierarchicalContainer fsContainer = null;

    private final Tree sourceTree = new Tree();

    public DashBoardPanel() {
        super();
        setSplitPosition(80);
        buildLeftSide();
    }

    private void buildRightSide() {
        sidePortal.setSizeFull();

        buildFileSystemContainer(null, 0);
        sourceTree.setContainerDataSource(fsContainer);
        sourceTree.setItemCaptionPropertyId("name");
        sourceTree.setSizeFull();
        sourceTree.addListener(new ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                if (!event.isDoubleClick())
                    return;

                Item item = event.getItem();
                if (item == null)
                    return;

                String path = item.getItemProperty("fullPath").getValue()
                        .toString();
                File f = new File(path);
                if (!f.isFile() || f.isDirectory())
                    return;
                Panel panel = new Panel();
                panel.setSizeFull();

                Label code = new Label(new TextFileProperty(f),
                        Label.CONTENT_XHTML);
                panel.addComponent(code);

                Iterator<Component> it = mainPortal.getComponentIterator();
                while (it.hasNext()) {
                    Component c = it.next();
                    if (!mainPortal.isCollapsed(c))
                        mainPortal.setCollapsed(c, true);
                }
                Component c = createTableTest();
                c.setHeight("30%");
                mainPortal.addComponent(panel);
                panel.setCaption(f.getName());
            }
        });
        sidePortal.addComponent(sourceTree);
        sidePortal.setLocked(sourceTree, true);
        sidePortal.setClosable(sourceTree, false);
        sourceTree.setCaption("Source Tree");
        addComponent(sidePortal);

        Label tx = new Label(
                "<b>Here You can browse some random source files double clicking their"
                        + " titles in the tree. You can notice three portals contained on the view: source browsing portal, "
                        + " table portal in the bottom and navigation portal on the right side. As source browsing portal created as "
                        + " not communicative - You cannot drag portlets out of it. The right and bottom portlets are fixed, so You can't"
                        + " drag them at all. They are also not closable.</b>",
                Label.CONTENT_XHTML);
        tx.setSizeFull();
        sidePortal.addComponent(tx);
        sidePortal.setLocked(tx, true);
        sidePortal.setClosable(tx, false);
        tx.setCaption("CONSOLE");
    }

    private void buildLeftSide() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(false, false, true, false);
        layout.setSpacing(true);
        mainPortal.setSizeFull();
        mainPortal.setSpacing(false);
        mainPortal.setCommunicative(false);
        bottomPortal.setSizeUndefined();
        bottomPortal.setWidth("100%");
        Component c = createTableTest();
        bottomPortal.addComponent(c);
        bottomPortal.setLocked(c, true);
        bottomPortal.setClosable(c, false);
        c.setCaption("Statistics");
        layout.addComponent(mainPortal);
        layout.addComponent(bottomPortal);
        layout.setExpandRatio(mainPortal, 1f);
        addComponent(layout);
    }

    private int buildFileSystemContainer(final String dirName, int parentId) {

        if (dirName == null) {
            fsContainer = new HierarchicalContainer();
            fsContainer.addContainerProperty("name", String.class, "");
            fsContainer.addContainerProperty("fullPath", String.class, "");
            Item item = fsContainer.addItem(parentId);
            String fullPath = getApplication().getContext().getBaseDirectory()
                    + "/SampleSources";
            item.getItemProperty("name").setValue("SampleSources");
            item.getItemProperty("fullPath").setValue(fullPath);
            fsContainer.setChildrenAllowed(parentId, true);
            return buildFileSystemContainer(fullPath, parentId);
        }

        File dir = new File(dirName);
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return true;
                String path = pathname.getAbsolutePath();
                int mid = path.lastIndexOf(".");
                String ext = path.substring(mid + 1, path.length());
                return "html".equals(ext);
            }
        });

        int currentId = parentId;

        for (final File file : files) {
            Item newItem = fsContainer.addItem(++currentId);
            newItem.getItemProperty("name").setValue(file.getName());
            newItem.getItemProperty("fullPath")
                    .setValue(file.getAbsolutePath());
            fsContainer.setParent(currentId, parentId);
            fsContainer.setChildrenAllowed(currentId, file.isDirectory());
            if (file.isDirectory())
                currentId += buildFileSystemContainer(file.getAbsolutePath(),
                        currentId);
        }

        return currentId;
    }

    public void populateTree() {
        if (fsContainer == null)
            buildRightSide();
    }

    public Table createTableTest() {
        final Table table = new Table("", new TestIndexedContainer());
        table.setSelectable(true);
        table.setWidth("100%");
        table.setHeight("200px");
        return table;
    }

    public class TestIndexedContainer extends IndexedContainer {
        public TestIndexedContainer() {
            super();
            addContainerProperty("test1", String.class, "0");
            addContainerProperty("test2", String.class, "0");
            for (int i = 0; i < 100; ++i) {
                Item item = getItem(addItem());
                item.getItemProperty("test1").setValue("test1");
                item.getItemProperty("test2").setValue("test2");
            }
        }
    }
}
