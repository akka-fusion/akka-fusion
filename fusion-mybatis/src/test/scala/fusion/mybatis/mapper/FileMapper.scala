package fusion.mybatis.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import fusion.mybatis.model.CFile

trait FileMapper extends BaseMapper[CFile] {
  def list(size: Int): java.util.List[CFile]
}
