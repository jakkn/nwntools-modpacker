/*
 * $Id$
 *
 * Copyright (c) 2004, Paul Speed
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3) Neither the names "Progeeks", "Meta-JB", nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.progeeks.nwn.ui;

import java.beans.IntrospectionException;
import java.io.*;
import java.util.*;

import org.progeeks.cmd.swing.SwingCommandProcessor;
import org.progeeks.meta.*;
import org.progeeks.meta.beans.*;
import org.progeeks.meta.swing.*;
import org.progeeks.meta.util.*;
import org.progeeks.meta.xml.*;

import org.progeeks.nwn.io.xml.*;
import org.progeeks.nwn.model.*;
import org.progeeks.nwn.resource.*;
import org.progeeks.util.*;
import org.progeeks.util.swing.*;

/**
 *  The model object for the application.  This is the root of the model
 *  hierarchy.
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class GlobalContext extends DefaultViewContext
{
    public static final String PROP_WINDOWS = "windows";

    private ObservableList windows = new ObservableList( new ArrayList() );

    private ResourceManager resourceMgr = new ResourceManager();

    private SwingCommandProcessor cmdProc = new SwingCommandProcessor();

    private FactoryRegistry factories = new FactoryRegistry();
    private MetaKit metaKit = BeanUtils.getMetaKit();

    /**
     *  Xml rendering engine that can write out project files, etc..
     */
    private XmlRenderingEngine xmlRenderer;

    public GlobalContext()
    {
        setupMetaClasses();
    }

    public String getApplication()
    {
        return( "Pandora" );
    }

    public String getVersion()
    {
        return( "v 0.0.1" );
    }

    public ResourceManager getResourceManager()
    {
        return( resourceMgr );
    }

    public SwingCommandProcessor getCommandProcessor()
    {
        return( cmdProc );
    }

    public FactoryRegistry getFactoryRegistry()
    {
        return( factories );
    }

    /**
     *  Setup any meta-classes.
     */
    protected void setupMetaClasses()
    {
        try
            {
            // Ideally, we'd really load this from a configuration file
            // somewhere.

            List properties = BeanUtils.getBeanPropertyInfos( Project.class );
            MetaObjectUtils.replacePropertyType( properties, "projectDescription", new LongStringType() );

            MetaClassRegistry.getRootRegistry().createMetaClass( Project.class.getName(), properties );

            MetaClass fiClass = BeanUtils.createBeanMetaClass( FileIndex.class );
            MetaClass graphClass = BeanUtils.createBeanMetaClass( ProjectGraph.class );
            MetaClass riClass = BeanUtils.createBeanMetaClass( ResourceIndex.class );
            BeanUtils.createBeanMetaClass( ProjectRoot.class );

            xmlRenderer = new XmlRenderingEngine();

            xmlRenderer.registerRenderer( graphClass, new GraphXmlRenderer() );

            // Register a custom editor for the FileIndex.
            // Right now, we don't use a correct one, but this keeps the
            // UI from looking and acting screwey.
            List fields = new ArrayList();
            fields.add( "fullPath" );
            registerForm( fiClass, fields );
            }
        catch( IntrospectionException e )
            {
            throw new RuntimeException( "Error building meta-class information", e );
            }
    }

    protected void registerForm( MetaClass metaClass, List fields )
    {
        MultiColumnPanelUIFactory factory = new MultiColumnPanelUIFactory( metaClass, fields );

        factories.registerEditorFactory( metaClass, factory );
        factories.registerRendererFactory( metaClass, factory );
    }

    /**
     *  Temporary method.
     */
    public void saveProject( Project project ) throws IOException
    {
        File f = project.getProjectFile();

        MetaClass cProject = metaKit.getMetaClassForObject( project );
        MetaObject mProject = metaKit.wrapObject( project, cProject );

        FileWriter out = new FileWriter( f );
        try
            {
            xmlRenderer.renderXml( mProject, out );
            }
        finally
            {
            out.close();
            }
    }

    /**
     *  Provides direct access to the observable window context list.
     */
    public ObservableList getWindowContexts()
    {
        return( windows );
    }
}
