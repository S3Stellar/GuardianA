package com.example.guardiana.utility;

import com.example.guardiana.R;

public final class ReportType {

    public static int getReportImage(ReportTypesEnum reportTypesEnum) {
        if (reportTypesEnum.equals(ReportTypesEnum.POLICE)) {
            return R.drawable.police_car;
        } else if (reportTypesEnum.equals(ReportTypesEnum.ACCIDENT)) {
            return R.drawable.accident;
        } else if (reportTypesEnum.equals(ReportTypesEnum.PROTEST)) {
            return R.drawable.protest;
        } else if (reportTypesEnum.equals(ReportTypesEnum.PUMP)){
            return R.drawable.pump;
        } else {
            return -1;
        }
    }
}