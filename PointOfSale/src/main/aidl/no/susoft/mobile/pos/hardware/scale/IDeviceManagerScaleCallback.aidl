package no.susoft.mobile.pos.hardware.scale;

interface IDeviceManagerScaleCallback {
	//
	oneway void notifyWeight(in int id, in long weight, in int status, in long price);

	//
	oneway void notifyLiveWeight(in int id, in long weight, in int status);
}

