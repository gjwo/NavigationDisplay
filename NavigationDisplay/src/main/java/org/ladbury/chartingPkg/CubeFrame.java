package org.ladbury.chartingPkg;

/*
 *  @(#)CubeFrame.java 1.0 98/11/09 15:07:04
 *
 * Copyright (c) 1996-1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.media.j3d.*;

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;
import dataTypes.Data3f;

//   CubeFrame renders a single, rotated cube.

public class CubeFrame extends Applet {

    public SimpleBehavior myRotationBehavior;

    public class SimpleBehavior extends Behavior {

        private TransformGroup targetTG;
        private Transform3D rotation = new Transform3D();
        private Data3f angles;

        SimpleBehavior(TransformGroup targetTG) {
            this.targetTG = targetTG;
        }
        public void initialize() {
            wakeupOn(new WakeupOnAWTEvent(KeyEvent.VK_F24));
        }

        public void setAngles(Data3f angles)
        {
            this.angles = angles;
            processStimulus(null);
        }

        public void processStimulus(Enumeration criteria)
        {
        rotation.rotX(Math.toRadians(angles.getY()));
        Transform3D y = new Transform3D();
        y.rotY(Math.toRadians(angles.getX()));
        Transform3D z = new Transform3D();
        z.rotZ(Math.toRadians(angles.getZ()));
        rotation.mul(y);
        rotation.mul(z);
        targetTG.setTransform(rotation);
        if (criteria!= null)wakeupOn(new WakeupOnAWTEvent(KeyEvent.VK_F24));
    }

} // end of class SimpleBehavior

    private BranchGroup createSceneGraph() {
        // Create the root of the branch graph
        BranchGroup objRoot = new BranchGroup();

        TransformGroup objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        objRoot.addChild(objRotate);
        objRotate.addChild(new ColorCube(0.4));

        myRotationBehavior = new SimpleBehavior(objRotate);
        myRotationBehavior.setSchedulingBounds(new BoundingSphere());
        objRoot.addChild(myRotationBehavior);

        // Let Java 3D perform optimizations on this scene graph.
        objRoot.compile();

        return objRoot;
    } // end of CreateSceneGraph method of SimpleBehaviorApp

    // Create a simple scene and attach it to the virtual universe

    public CubeFrame() {
        setLayout(new BorderLayout());
        Canvas3D canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        add("Center", canvas3D);

        BranchGroup scene = createSceneGraph();

        // SimpleUniverse is a Convenience Utility class
        SimpleUniverse simpleU = new SimpleUniverse(canvas3D);

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
        simpleU.getViewingPlatform().setNominalViewingTransform();

        simpleU.addBranchGraph(scene);
    } // end of SimpleBehaviorApp (constructor)

    //  The following allows this to be run as an application
    //  as well as an applet

} // end of class CubeFrame


