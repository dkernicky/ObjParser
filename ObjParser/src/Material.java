public class Material {
	protected float ka[], kd[], ks[];
	protected int ns;
	protected String name;

	public Material() {
	}

	public Material(float ka[], float kd[], float ks[], int ns, String name) {
		super();
		this.ka = ka;
		this.kd = kd;
		this.ks = ks;
		this.ns = ns;
		this.name = name;
	}
}
