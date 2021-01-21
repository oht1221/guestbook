package org.basecamp.guestbook.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.basecamp.guestbook.dto.PageRequestDTO;
import org.basecamp.guestbook.dto.PageResultDTO;
import org.basecamp.guestbook.entity.QGuestbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.basecamp.guestbook.entity.Guestbook;
import org.basecamp.guestbook.dto.GuestbookDTO;
import org.basecamp.guestbook.repository.GuestbookRepository;

import java.util.Optional;
import java.util.function.Function;


@Service
@Log4j2
@RequiredArgsConstructor
public class GuestbookServiceImpl implements  GuestbookService{

    private final GuestbookRepository repository;

    @Override
    public Long register(GuestbookDTO dto){

        log.info("DTO---------------");
        log.info(dto);

        Guestbook entity = dtoToEntity(dto);

        log.info(entity);

        repository.save(entity);

        return entity.getGno();
    }

    public PageResultDTO<GuestbookDTO, Guestbook> getList(PageRequestDTO requestDTO){

        Pageable pageable = requestDTO.getPageable(Sort.by("gno").descending());

        BooleanBuilder booleanBuilder = getSearch(requestDTO);

        Page<Guestbook> result = repository.findAll(booleanBuilder, pageable);

        Function<Guestbook, GuestbookDTO> fn = (entity -> entityToDto(entity));

        return new PageResultDTO<>(result, fn);
    }

    public GuestbookDTO read(Long gno){
        Optional<Guestbook> result = repository.findById(gno);

        return result.isPresent()? entityToDto(result.get()): null; //get이 Optional에서 Guestbook entity로 변환시켜줌
    }

    public void remove(Long gno){

        repository.deleteById(gno);

    }

    public void modify(GuestbookDTO dto){

        Optional<Guestbook> result = repository.findById(dto.getGno());

        if(result.isPresent()){

            Guestbook entity= result.get();

            entity.changeTitle(dto.getTitle());
            entity.changeContent(dto.getContent());

            repository.save(entity);
        }
    }

    private BooleanBuilder getSearch(PageRequestDTO requestDTO){

        String type = requestDTO.getType();

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        QGuestbook qGuestbook = QGuestbook.guestbook;

        String keyword = requestDTO.getKeyword();

        BooleanExpression expression = qGuestbook.gno.gt(0L);

        booleanBuilder.and(expression);

        if(type == null || type.trim().length() == 0){ // 검색 조건 없음
            return booleanBuilder;
        }

        BooleanBuilder conditionBuilder = new BooleanBuilder();
        //조건 생성
        if(type.contains("t")){
            conditionBuilder.or(qGuestbook.title.contains(keyword));
        }
        if(type.contains("c")){
            conditionBuilder.or(qGuestbook.content.contains(keyword));
        }
        if(type.contains("w")){
            conditionBuilder.or(qGuestbook.content.contains(keyword));
        }

        booleanBuilder.and(conditionBuilder);

        return booleanBuilder;
    }
}
