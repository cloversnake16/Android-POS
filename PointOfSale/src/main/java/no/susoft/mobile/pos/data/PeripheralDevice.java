package no.susoft.mobile.pos.data;

import com.bxl.BXLConst;

public class PeripheralDevice {

    private String name;
    private PeripheralType type;
    private PeripheralProvider provider;
    private String ip;
    private boolean paired;
    private boolean used;

	public static String[] deviceNames = new String[]{"CASIO", "NETS IP", "VERIFONE PIM"};
	public static String[] bxlDevices = new String[]{BXLConst.SRP_350PLUSIII, BXLConst.SRP_275III};

    public PeripheralDevice(String name, boolean paired, boolean used) {
        this.name = name;
        this.paired = paired;
        this.used = used;

        this.type = PeripheralType.NONE;
        this.provider = PeripheralProvider.NONE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PeripheralType getType() {
        return type;
    }

    public void setType(PeripheralType type) {
        this.type = type;
    }

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }


    public PeripheralProvider getProvider() {
        return provider;
    }

    public void setProvider(PeripheralProvider provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeripheralDevice that = (PeripheralDevice) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
