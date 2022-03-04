package domain.target;

import com.google.common.net.InternetDomainName;

import burp.DomainNameUtils;
import domain.DomainPanel;

public class TargetEntry {
	private String target;//根域名、网段、或者IP
	private String type;
	private String keyword = "";
	private String AuthoritativeNameServer;
	private boolean ZoneTransfer = false;//域名对应的权威服务器，是否域于传送漏洞
	private boolean isBlack = false;//这个域名是否是黑名单根域名，需要排除的
	private String comment = "";
	private boolean useTLD;//TLD= Top-Level Domain
	
	public static final String Target_Type_Domain = "Domain";
	public static final String Target_Type_Subnet = "Subnet";
	public static final String Target_Type_IPaddress = "IP";

	public static final String[]  TargetTypeArray = {Target_Type_Domain,Target_Type_Subnet,Target_Type_IPaddress};
	
	
	public TargetEntry(String input) {
		this(input,true);
	}
	public TargetEntry(String input,boolean autoSub) {
		
		String domain = DomainNameUtils.cleanDomain(input);
		if (domain != null && DomainNameUtils.isValidDomain(domain)) {
			target = domain;
			type = Target_Type_Domain;
			
			if (autoSub) {
				String RootDomain = InternetDomainName.from(domain).topPrivateDomain().toString();
				target = RootDomain;
			}
	        keyword = domain.substring(0, domain.indexOf("."));
	        
	        try {
				DomainPanel.getDomainResult().getSubDomainSet().add(domain);
				DomainPanel.getDomainResult().getRelatedDomainSet().remove(domain);//刷新时不能清空，所有要有删除操作。
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
		}
		//IP

	}
	
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getAuthoritativeNameServer() {
		return AuthoritativeNameServer;
	}
	public void setAuthoritativeNameServer(String authoritativeNameServer) {
		AuthoritativeNameServer = authoritativeNameServer;
	}

	public boolean isZoneTransfer() {
		return ZoneTransfer;
	}
	public void setZoneTransfer(boolean zoneTransfer) {
		ZoneTransfer = zoneTransfer;
	}
	public boolean isBlack() {
		return isBlack;
	}
	public void setBlack(boolean isBlack) {
		this.isBlack = isBlack;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isUseTLD() {
		return useTLD;
	}

	public void setUseTLD(boolean useTLD) {
		this.useTLD = useTLD;
	}
}
