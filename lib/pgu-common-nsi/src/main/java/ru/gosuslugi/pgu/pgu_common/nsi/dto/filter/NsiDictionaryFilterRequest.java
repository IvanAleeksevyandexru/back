package ru.gosuslugi.pgu.pgu_common.nsi.dto.filter;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class NsiDictionaryFilterRequest {
    String treeFiltering;
    String pageNum;
    String pageSize;
    NsiDictionaryFilter filter;
    String parentRefItemValue;
    List<String> selectAttributes;
    String tx;


    public static class Builder {
        private static final String DEFAULT_SELECTED_ATTR = "*";

        private String treeFiltering;
        private String pageNum;
        private String pageSize;
        private String parentRefItemValue;
        private NsiDictionaryFilter filter;
        private List<String> selectAttributes;

        public Builder setTreeFiltering(String treeFiltering) {
            this.treeFiltering = treeFiltering;
            return this;
        }

        public Builder setPageNum(String pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder setPageSize(String pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder setSelectAttributes(List<String> selectAttributes) {
            this.selectAttributes = selectAttributes;
            return this;
        }

        public Builder setFilter(NsiDictionaryFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder setParentRefItemValue(String parentRefItemValue) {
            this.parentRefItemValue = parentRefItemValue;
            return this;
        }

        public NsiDictionaryFilterRequest build() {
            NsiDictionaryFilterRequest requestBody = new NsiDictionaryFilterRequest();

            if (this.selectAttributes == null) {
                this.selectAttributes = Collections.singletonList(DEFAULT_SELECTED_ATTR);
            }
            requestBody.setSelectAttributes(this.selectAttributes);
            if (this.treeFiltering == null) {
                this.treeFiltering = NsiDictionaryTreeFilterType.ONELEVEL.name();
            }
            requestBody.setTreeFiltering(this.treeFiltering);
            requestBody.setPageNum(this.pageNum);
            requestBody.setPageSize(this.pageSize);
            requestBody.setFilter(filter);
            requestBody.setParentRefItemValue(parentRefItemValue);

            return requestBody;
        }
    }
}

