package com.eucalyptus.reporting.art.renderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import com.eucalyptus.reporting.art.entity.AccountArtEntity;
import com.eucalyptus.reporting.art.entity.AvailabilityZoneArtEntity;
import com.eucalyptus.reporting.art.entity.BucketArtEntity;
import com.eucalyptus.reporting.art.entity.ReportArtEntity;
import com.eucalyptus.reporting.art.entity.S3ObjectUsageArtEntity;
import com.eucalyptus.reporting.art.entity.UserArtEntity;
import com.eucalyptus.reporting.art.entity.VolumeArtEntity;
import com.eucalyptus.reporting.art.entity.VolumeUsageArtEntity;
import com.eucalyptus.reporting.art.renderer.document.Document;
import com.eucalyptus.reporting.units.SizeUnit;
import com.eucalyptus.reporting.units.TimeUnit;
import com.eucalyptus.reporting.units.UnitUtil;
import com.eucalyptus.reporting.units.Units;

class S3Renderer
	implements Renderer
{
	private Document doc;
	
	public S3Renderer(Document doc)
	{
		this.doc = doc;
	}
	
	
	@Override
	public void render(ReportArtEntity report, OutputStream os, Units units)
			throws IOException
	{
        doc.setWriter(new OutputStreamWriter(os));
        
        doc.open();
        doc.textLine("S3 Report", 1);
        doc.textLine("Begin:" + new Date(report.getBeginMs()).toString(), 4);
        doc.textLine("End:" + new Date(report.getEndMs()).toString(), 4);
        doc.textLine("Resource Usage Section", 3);
        doc.tableOpen();

        doc.newRow().addValCol("Bucket").addValCol("# Objects")
        	.addValCol("Total Obj Size (" + units.getSizeUnit().toString() + ")").addValCol("Obj GB-Days");
        for(String zoneName : report.getZones().keySet()) {
        	AvailabilityZoneArtEntity zone = report.getZones().get(zoneName);
            doc.newRow().addLabelCol(0, "Zone: " + zoneName).addValCol("cumul.");
            addUsageCols(doc, zone.getUsageTotals().getS3ObjectTotals(), units);
            for (String accountName: zone.getAccounts().keySet()) {
              	AccountArtEntity account = zone.getAccounts().get(accountName);
                doc.newRow().addLabelCol(1, "Account: " + accountName).addValCol("cumul.");
                addUsageCols(doc, account.getUsageTotals().getS3ObjectTotals(),units);
                for (String userName: account.getUsers().keySet()) {
                   	UserArtEntity user = account.getUsers().get(userName);
                       doc.newRow().addLabelCol(2, "User: " + userName)
                       		.addValCol("cumul.");
                       addUsageCols(doc, user.getUsageTotals().getS3ObjectTotals(),units);
                    for (String bucketName: user.getBuckets().keySet()) {
                    	BucketArtEntity bucket = user.getBuckets().get(bucketName);
                       	doc.newRow().addValCol(bucketName);
                       	addUsageCols(doc, bucket.getTotalUsage(), units);
                    }
                }
            }
        }
        doc.tableClose();
        doc.close();
     
	}

	public static Document addUsageCols(Document doc, S3ObjectUsageArtEntity entity, Units units)
		throws IOException
	{
		doc.addValCol(entity.getObjectsNum());
		doc.addValCol(UnitUtil.convertSize(entity.getSizeGB(), SizeUnit.GB, units.getSizeUnit()));
		doc.addValCol(UnitUtil.convertSizeTime(entity.getGBsecs(), SizeUnit.GB,
				units.getSizeUnit(), TimeUnit.SECS, units.getTimeUnit()));
		return doc;
	}

}
