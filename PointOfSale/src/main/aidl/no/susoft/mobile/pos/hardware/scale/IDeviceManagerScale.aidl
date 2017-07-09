package no.susoft.mobile.pos.hardware.scale;

import no.susoft.mobile.pos.hardware.scale.IDeviceManagerScaleCallback;

interface IDeviceManagerScale {
	//
	int open(IDeviceManagerScaleCallback cb);

	//
	int close(in int id);

	//
	int setProperty(in int id, in Map property);

	//
	Map getProperty(in int id);

	//
	int liveWeight(in int id, boolean enable);

	//
	int readWeight(in int id, in int timeout);

	//
	int zeroScale(in int id);

	//
	int getStatus(in int id);

	//
	List<String> command(in int id, in List<String> cmd);
}

