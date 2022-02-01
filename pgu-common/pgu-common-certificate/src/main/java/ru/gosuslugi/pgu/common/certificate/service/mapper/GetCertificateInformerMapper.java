package ru.gosuslugi.pgu.common.certificate.service.mapper;

import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.CertificateRecipientData;
import ru.gosuslugi.pgu.common.certificate.xsd.dto.GetCertificateInformer;

import java.util.List;
import java.util.Map;

@Mapper
public abstract class GetCertificateInformerMapper {

    public static final GetCertificateInformerMapper INSTANCE = Mappers.getMapper(GetCertificateInformerMapper.class);

    protected List<GetCertificateInformer.RecipientDataForGUID> forGUIDList;

    @BeforeMapping
    protected void init(Map<String, String> componentArguments) {
        CertificateRecipientData recipientData = CertificateRecipientDataMapper.INSTANCE.convert(componentArguments);
        GetCertificateInformer.RecipientDataForGUID dataForGUID = new GetCertificateInformer.RecipientDataForGUID();
        dataForGUID.setRecipientBirthCertificateData(recipientData.getRecipientBirthCertificateData());
        dataForGUID.setRecipientSNILS(recipientData.getRecipientSNILS());
        forGUIDList = new GetCertificateInformer().getRecipientDataForGUIDs();
        forGUIDList.add(dataForGUID);
    }

    @Mapping(target = "recipientDataForGUIDs", expression = "java(forGUIDList)")
    public abstract GetCertificateInformer convert(Map<String, String> componentArguments);

}
