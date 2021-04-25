package com.thulium.entity;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;

public class Cable {
	public Cable() {
	}
	
	public RopeJointDef getBodyDef(Body bodyA, Body bodyB) {
		RopeJointDef ropeDef = new RopeJointDef();
		ropeDef.bodyA = bodyA;
		ropeDef.bodyB = bodyB;
		ropeDef.maxLength = 10;
		ropeDef.type = JointType.RopeJoint;
		return ropeDef;
	}
}
