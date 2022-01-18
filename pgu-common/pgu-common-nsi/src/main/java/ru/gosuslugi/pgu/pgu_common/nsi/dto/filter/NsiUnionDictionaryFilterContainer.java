package ru.gosuslugi.pgu.pgu_common.nsi.dto.filter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NsiUnionDictionaryFilterContainer implements NsiDictionaryFilter {
    private NsiDictionaryFilterUnion union;

    public static class Builder {
        private NsiDictionaryUnionType nsiDictionaryUnionType;
        private List<NsiDictionaryFilterSimple.Builder> filterBuilders;

        public Builder setNsiDictionaryUnionType(NsiDictionaryUnionType nsiDictionaryUnionType) {
            this.nsiDictionaryUnionType = nsiDictionaryUnionType;
            return this;
        }

        public Builder setFilterBuilders(List<NsiDictionaryFilterSimple.Builder> filterBuilders) {
            this.filterBuilders = filterBuilders;
            return this;
        }

        public NsiUnionDictionaryFilterContainer build() {
            NsiUnionDictionaryFilterContainer filter = new NsiUnionDictionaryFilterContainer();
            List<NsiDictionaryFilter> simples = new ArrayList<>();
            NsiDictionaryFilterUnion union = new NsiDictionaryFilterUnion();
            filter.setUnion(union);
            union.setSubs(simples);
            union.setUnionKind(this.nsiDictionaryUnionType);
            List<NsiDictionaryFilter> subs = new ArrayList<>();
            union.setSubs(subs);
            this.filterBuilders.forEach(
                    builder -> {
                        NsiSimpleDictionaryFilterContainer container = new NsiSimpleDictionaryFilterContainer();
                        container.setSimple(builder.build());
                        subs.add(container);
                    }
            );
            filter.setUnion(union);
            return filter;
        }
    }
}
