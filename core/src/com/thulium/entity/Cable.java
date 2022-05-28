package com.thulium.entity;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;

public class Cable {
	private RopeJoint joint;

	public Cable() {
	}
	
	public void update() {
		
	}

	public RopeJoint getJoint() {
		return joint;
	}

	public void setJoint(Joint joint) {
		this.joint = (RopeJoint) joint;
	}
	
	public RopeJointDef getBodyDef(Body bodyA, Body bodyB) {
		RopeJointDef ropeDef = new RopeJointDef();
		ropeDef.bodyA = bodyA;
		ropeDef.bodyB = bodyB;
		ropeDef.maxLength = 5;
		ropeDef.localAnchorA.set(bodyA.getLocalCenter());
		ropeDef.localAnchorB.set(bodyB.getLocalCenter());
		ropeDef.collideConnected = true;
		ropeDef.type = JointType.RopeJoint;
		return ropeDef;
	}
}
